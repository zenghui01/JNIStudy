# Andfix 原理分析

---

## 即时生效

`App` 启动到一半的时候，所有需要发生变更的类已经被加载过了，
在 `Android` 系统中是无法对一个已经加载的类进行卸载的。腾讯的 `Tinker` 的方案是让 `ClassLoader` 去加载新的类，如果不重启 `App`，原有的类还在虚拟机中，就无法加载新的类。因此需要冷启动后，抢先加载修复补丁中的新类，从而达到热修复的目的。

`AndFix` 采用的方法是直接在已加载的类中的 `native` 层替换掉原方法，是在原有类的基础上进行修改的。

## 底层替换原理

每一个 `Java` 方法在 `Art` 虚拟机中都对应一个 [**ArtMethod**](https://zhuanlan.zhihu.com/p/54848337)，[**ArtMethod**](https://zhuanlan.zhihu.com/p/54848337) 记录了该方法的所有信息，包括所属类、访问权限、代码执行地址等。

通过`env->FromReflectedMethod`，可以由 `Method` 对象得到这个方法对应的ArtMethod的真正起始地址，然后强转为 **ArtMethod** 指针，通过指针的操作对其成员属性进行修改替换。

> 为什么这样替换后就可以实现热修复呢？
**就需要先了解虚拟机方法调用的原理**

## 虚拟机方法调用的原理
Android 5.1.1 版本中的 **ArtMethod** 结构体路径为：
`https://www.androidos.net.cn/android/5.1.0_r3/xref/art/runtime/mirror/art_method.h`。其中最重要的字段就是 `entry_point_from_interpreter_` 和 `entry_point_from_quick_compiled_code_` ，从名字可以看出它们就是方法执行的入口。

Java 代码在 Android 中会被编译成 Dex code，
Art虚拟机中可以采用解释模式或AOT机器码模式执行 Dex Code。

 - 解释模式：就是取出 Dex Code，逐条解释执行。如果方法的调用者是以解释模式运行的，调用该方法时，就会获取它的 `entry_point_from_interpreter_` ,然后跳转执行。
 - AOT 模式：就会预编译 Dex code 对应的机器码，然后在运行期间直接执行机器码，不需要逐条解释执行 dex code。如果方法的调用者是以 AOT 机器码方式执行的，在调用该方法时就是跳转到 `entry_point_from_quick_compiled_code_` 中执行的。
 

> 那是不是替换方法的执行入口就可以了呢？
**当然不是，无论是解释模式还是 AOT 机器码模式，在运行期间还会需要调用 ArtMethod 中的其他成员字段**

## 源码跟踪验证

### 类加载流程

>  1. FindClass (/art/runtime/jni_internal.cc)
>  2. FindClass (/art/runtime/class_linker.cc)
>  3. DefineClass (/art/runtime/class_linker.cc)
>  4. LoadClass (/art/runtime/class_linker.cc)
>  5. LoadClassMembers (/art/runtime/class_linker.cc)
>  6. LinkCode (/art/runtime/class_linker.cc)
>  7. LinkMethod (/art/runtime/oat_file.cc)
>  8. NeedsInterpreter (/art/runtime/class_linker.cc)
>  9. UnregisterNative(/art/runtime/mirror/art_method.cc)
>  10. UpdateMethodsCode(/art/runtime/instrumentation.cc)
>  11. UpdateEntrypoints(/art/runtime/instrumentation.cc)

#### JNI 类的静态成员函数 **FindClass**
```
static jclass FindClass(JNIEnv* env, const char* name) {
    CHECK_NON_NULL_ARGUMENT(name);
    Runtime* runtime = Runtime::Current();
    ClassLinker* class_linker = runtime->GetClassLinker();
    std::string descriptor(NormalizeJniClassDescriptor(name));
    ScopedObjectAccess soa(env);
    mirror::Class* c = nullptr;
    if (runtime->IsStarted()) {
      StackHandleScope<1> hs(soa.Self());
      Handle<mirror::ClassLoader> class_loader(hs.NewHandle(GetClassLoader(soa)));
      c = class_linker->FindClass(soa.Self(), descriptor.c_str(), class_loader);
    } else {
      c = class_linker->FindSystemClass(soa.Self(), descriptor.c_str());
    }
    return soa.AddLocalReference<jclass>(c);
  }
```
在ART虚拟机进程中，存在着一个 Runtime 单例，用来描述 ART 运行时。通过调用 Runtime 类的静态成员函数 Current 可以获得上述 Runtime 单例。获得了这个单例之后，就可以调用它的成员函数 GetClassLinker 来获得一个 **ClassLinker** 对象。

> ClassLinker 对象是在创建 ART 虚拟机的过程中创建的，用来加载类以及链接类方法

首先判断 ART 运行时是否已经启动起来。如果已经启动，那么就通过调用函数 GetClassLoader 来获得当前线程所关联的 ClassLoader，并且以此为参数，调用前面获得的 ClassLinker 对象的成员函数 FindClass 来加载由参数 name 指定的类。

如果 ART 运行时还没有启动，那么这时候只可以加载系统类。这个通过前面获得的 ClassLinker 对象的成员函数 FindSystemClass 来实现的。

#### ClassLinker类的成员函数 **FindClass**
```
mirror::Class* ClassLinker::FindClass(Thread* self, const char* descriptor,
                                      Handle<mirror::ClassLoader> class_loader) {
  DCHECK_NE(*descriptor, '\0') << "descriptor is empty string";
  DCHECK(self != nullptr);
  self->AssertNoPendingException();
  if (descriptor[1] == '\0') {
    // only the descriptors of primitive types should be 1 character long, also avoid class lookup
    // for primitive classes that aren't backed by dex files.
    return FindPrimitiveClass(descriptor[0]);
  }
  const size_t hash = ComputeModifiedUtf8Hash(descriptor);
  // Find the class in the loaded classes table.
  mirror::Class* klass = LookupClass(descriptor, hash, class_loader.Get());
  if (klass != nullptr) {
    return EnsureResolved(self, descriptor, klass);
  }
  // Class is not yet loaded.
  if (descriptor[0] == '[') {
    return CreateArrayClass(self, descriptor, hash, class_loader);
  } else if (class_loader.Get() == nullptr) {
    // The boot class loader, search the boot class path.
    ClassPathEntry pair = FindInClassPath(descriptor, hash, boot_class_path_);
    if (pair.second != nullptr) {
      return DefineClass(self, descriptor, hash, NullHandle<mirror::ClassLoader>(), *pair.first,
                         *pair.second);
    } else {
      // The boot class loader is searched ahead of the application class loader, failures are
      // expected and will be wrapped in a ClassNotFoundException. Use the pre-allocated error to
      // trigger the chaining with a proper stack trace.
      mirror::Throwable* pre_allocated = Runtime::Current()->GetPreAllocatedNoClassDefFoundError();
      self->SetException(ThrowLocation(), pre_allocated);
      return nullptr;
    }
  } else if (Runtime::Current()->UseCompileTimeClassPath()) {
    // First try with the bootstrap class loader.
    if (class_loader.Get() != nullptr) {
      klass = LookupClass(descriptor, hash, nullptr);
      if (klass != nullptr) {
        return EnsureResolved(self, descriptor, klass);
      }
    }
    // If the lookup failed search the boot class path. We don't perform a recursive call to avoid
    // a NoClassDefFoundError being allocated.
    ClassPathEntry pair = FindInClassPath(descriptor, hash, boot_class_path_);
    if (pair.second != nullptr) {
      return DefineClass(self, descriptor, hash, NullHandle<mirror::ClassLoader>(), *pair.first,
                         *pair.second);
    }
    // Next try the compile time class path.
    const std::vector<const DexFile*>* class_path;
    {
      ScopedObjectAccessUnchecked soa(self);
      ScopedLocalRef<jobject> jclass_loader(soa.Env(),
                                            soa.AddLocalReference<jobject>(class_loader.Get()));
      class_path = &Runtime::Current()->GetCompileTimeClassPath(jclass_loader.get());
    }
    pair = FindInClassPath(descriptor, hash, *class_path);
    if (pair.second != nullptr) {
      return DefineClass(self, descriptor, hash, class_loader, *pair.first, *pair.second);
    }
  } else {
    ScopedObjectAccessUnchecked soa(self);
    mirror::Class* klass = FindClassInPathClassLoader(soa, self, descriptor, hash, class_loader);
    if (klass != nullptr) {
      return klass;
    }
    ScopedLocalRef<jobject> class_loader_object(soa.Env(),
                                                soa.AddLocalReference<jobject>(class_loader.Get()));
    std::string class_name_string(DescriptorToDot(descriptor));
    ScopedLocalRef<jobject> result(soa.Env(), nullptr);
    {
      ScopedThreadStateChange tsc(self, kNative);
      ScopedLocalRef<jobject> class_name_object(soa.Env(),
                                                soa.Env()->NewStringUTF(class_name_string.c_str()));
      if (class_name_object.get() == nullptr) {
        DCHECK(self->IsExceptionPending());  // OOME.
        return nullptr;
      }
      CHECK(class_loader_object.get() != nullptr);
      result.reset(soa.Env()->CallObjectMethod(class_loader_object.get(),
                                               WellKnownClasses::java_lang_ClassLoader_loadClass,
                                               class_name_object.get()));
    }
    if (self->IsExceptionPending()) {
      // If the ClassLoader threw, pass that exception up.
      return nullptr;
    } else if (result.get() == nullptr) {
      // broken loader - throw NPE to be compatible with Dalvik
      ThrowNullPointerException(nullptr, StringPrintf("ClassLoader.loadClass returned null for %s",
                                                      class_name_string.c_str()).c_str());
      return nullptr;
    } else {
      // success, return mirror::Class*
      return soa.Decode<mirror::Class*>(result.get());
    }
  }

  ThrowNoClassDefFoundError("Class %s not found", PrintableString(descriptor).c_str());
  return nullptr;
}
```

参数 descriptor 指向的是要加载的类的签名，而参数 class_loader 指向的是一个类加载器。

首先是调用另外一个成员函数 LookupClass 来检查参数 descriptor 指定的类是否已经被加载过。如果是的话，那么 ClassLinker 类的成员函数 LookupClass 就会返回一个对应的 Class 对象，这个 Class 对象接着就会返回给调用者，表示加载已经完成。

如果参数 descriptor 指定的类还没有被加载过，这时候主要就是要看参数 class_loader 的值了。如果参数 class_loader 的值等于 NULL，那么就需要调用 FindInClassPath 来在系统启动类路径寻找对应的类。一旦寻找到，那么就会获得包含目标类的DEX文件，因此接下来就调用 ClassLinker 类的另外一个成员函数 **DefineClass** 从获得的DEX文件中加载参数 descriptor 指定的类了。

> 知道了参数 descriptor 指定的类定义在哪一个 DEX 文件之后，就可以通过 ClassLinker 类的另外一个成员函数
> **DefineClass** 来从中加载它了。

#### ClassLinker 类的成员函数 **DefineClass**

```
mirror::Class* ClassLinker::DefineClass(Thread* self, const char* descriptor, size_t hash,
                                        Handle<mirror::ClassLoader> class_loader,
                                        const DexFile& dex_file,
                                        const DexFile::ClassDef& dex_class_def) {
  StackHandleScope<3> hs(self);
  auto klass = hs.NewHandle<mirror::Class>(nullptr);

  // Load the class from the dex file.
  if (UNLIKELY(!init_done_)) {
    // finish up init of hand crafted class_roots_
    if (strcmp(descriptor, "Ljava/lang/Object;") == 0) {
      klass.Assign(GetClassRoot(kJavaLangObject));
    } else if (strcmp(descriptor, "Ljava/lang/Class;") == 0) {
      klass.Assign(GetClassRoot(kJavaLangClass));
    } else if (strcmp(descriptor, "Ljava/lang/String;") == 0) {
      klass.Assign(GetClassRoot(kJavaLangString));
    } else if (strcmp(descriptor, "Ljava/lang/ref/Reference;") == 0) {
      klass.Assign(GetClassRoot(kJavaLangRefReference));
    } else if (strcmp(descriptor, "Ljava/lang/DexCache;") == 0) {
      klass.Assign(GetClassRoot(kJavaLangDexCache));
    } else if (strcmp(descriptor, "Ljava/lang/reflect/ArtField;") == 0) {
      klass.Assign(GetClassRoot(kJavaLangReflectArtField));
    } else if (strcmp(descriptor, "Ljava/lang/reflect/ArtMethod;") == 0) {
      klass.Assign(GetClassRoot(kJavaLangReflectArtMethod));
    }
  }

  if (klass.Get() == nullptr) {
    // Allocate a class with the status of not ready.
    // Interface object should get the right size here. Regular class will
    // figure out the right size later and be replaced with one of the right
    // size when the class becomes resolved.
    klass.Assign(AllocClass(self, SizeOfClassWithoutEmbeddedTables(dex_file, dex_class_def)));
  }
  if (UNLIKELY(klass.Get() == nullptr)) {
    CHECK(self->IsExceptionPending());  // Expect an OOME.
    return nullptr;
  }
  klass->SetDexCache(FindDexCache(dex_file));
  LoadClass(dex_file, dex_class_def, klass, class_loader.Get());
  ObjectLock<mirror::Class> lock(self, klass);
  if (self->IsExceptionPending()) {
    // An exception occured during load, set status to erroneous while holding klass' lock in case
    // notification is necessary.
    if (!klass->IsErroneous()) {
      klass->SetStatus(mirror::Class::kStatusError, self);
    }
    return nullptr;
  }
  klass->SetClinitThreadId(self->GetTid());

  // Add the newly loaded class to the loaded classes table.
  mirror::Class* existing = InsertClass(descriptor, klass.Get(), hash);
  if (existing != nullptr) {
    // We failed to insert because we raced with another thread. Calling EnsureResolved may cause
    // this thread to block.
    return EnsureResolved(self, descriptor, existing);
  }

  // Finish loading (if necessary) by finding parents
  CHECK(!klass->IsLoaded());
  if (!LoadSuperAndInterfaces(klass, dex_file)) {
    // Loading failed.
    if (!klass->IsErroneous()) {
      klass->SetStatus(mirror::Class::kStatusError, self);
    }
    return nullptr;
  }
  CHECK(klass->IsLoaded());
  // Link the class (if necessary)
  CHECK(!klass->IsResolved());
  // TODO: Use fast jobjects?
  auto interfaces = hs.NewHandle<mirror::ObjectArray<mirror::Class>>(nullptr);

  mirror::Class* new_class = nullptr;
  if (!LinkClass(self, descriptor, klass, interfaces, &new_class)) {
    // Linking failed.
    if (!klass->IsErroneous()) {
      klass->SetStatus(mirror::Class::kStatusError, self);
    }
    return nullptr;
  }
  self->AssertNoPendingException();
  CHECK(new_class != nullptr) << descriptor;
  CHECK(new_class->IsResolved()) << descriptor;

  Handle<mirror::Class> new_class_h(hs.NewHandle(new_class));

  /*
   * We send CLASS_PREPARE events to the debugger from here.  The
   * definition of "preparation" is creating the static fields for a
   * class and initializing them to the standard default values, but not
   * executing any code (that comes later, during "initialization").
   *
   * We did the static preparation in LinkClass.
   *
   * The class has been prepared and resolved but possibly not yet verified
   * at this point.
   */
  Dbg::PostClassPrepare(new_class_h.Get());

  return new_class_h.Get();
}
```

ClassLinker 类有一个类型为 bool 的成员变量 init_done_，用来表示 ClassLinker 是否已经初始化完成。

如果 ClassLinker 正处于初始化过程，即其成员变量 init_done_ 的值等于 false，并且参数 descriptor 描述的是特定的内部类，那么就将本地变量 klass 指向它们，其余情况则会通过成员函数 AllocClass 为其分配存储空间，以便后面通过成员函数 **LoadClass** 进行初始化。

ClassLinker 类的成员函数 **LoadClass** 用来从指定的 DEX 文件中加载指定的类。指定的类从 DEX 文件中加载完成后，需要通过另外一个成员函数 InsertClass 添加到 ClassLinker 的已加载类列表中去。如果指定的类之前已经加载过，即调用成员函数 InsertClass 得到的返回值不等于空，那么就说明有另外的一个线程也正在加载指定的类。这时候就需要调用成员函数 EnsureResolved 来保证（等待）该类已经加载并且解析完成。另一方面，如果没有其它线程加载指定的类，那么当前线程从指定的 DEX 文件加载完成指定的类后，还需要调用成员函数 LinkClass 来对加载后的类进行解析。最后，一个类型为 Class 的对象就可以返回给调用者了，用来表示一个已经加载和解析完成的类。

#### ClassLinker 类的成员函数 **LoadClass**

```
void ClassLinker::LoadClass(const DexFile& dex_file,
                            const DexFile::ClassDef& dex_class_def,
                            Handle<mirror::Class> klass,
                            mirror::ClassLoader* class_loader) {
  CHECK(klass.Get() != nullptr);
  CHECK(klass->GetDexCache() != nullptr);
  CHECK_EQ(mirror::Class::kStatusNotReady, klass->GetStatus());
  const char* descriptor = dex_file.GetClassDescriptor(dex_class_def);
  CHECK(descriptor != nullptr);

  klass->SetClass(GetClassRoot(kJavaLangClass));
  if (kUseBakerOrBrooksReadBarrier) {
    klass->AssertReadBarrierPointer();
  }
  uint32_t access_flags = dex_class_def.GetJavaAccessFlags();
  CHECK_EQ(access_flags & ~kAccJavaFlagsMask, 0U);
  klass->SetAccessFlags(access_flags);
  klass->SetClassLoader(class_loader);
  DCHECK_EQ(klass->GetPrimitiveType(), Primitive::kPrimNot);
  klass->SetStatus(mirror::Class::kStatusIdx, nullptr);

  klass->SetDexClassDefIndex(dex_file.GetIndexForClassDef(dex_class_def));
  klass->SetDexTypeIndex(dex_class_def.class_idx_);
  CHECK(klass->GetDexCacheStrings() != nullptr);

  const byte* class_data = dex_file.GetClassData(dex_class_def);
  if (class_data == nullptr) {
    return;  // no fields or methods - for example a marker interface
  }

  OatFile::OatClass oat_class;
  if (Runtime::Current()->IsStarted()
      && !Runtime::Current()->UseCompileTimeClassPath()
      && FindOatClass(dex_file, klass->GetDexClassDefIndex(), &oat_class)) {
    LoadClassMembers(dex_file, class_data, klass, class_loader, &oat_class);
  } else {
    LoadClassMembers(dex_file, class_data, klass, class_loader, nullptr);
  }
}
```

> dex_file: 类型为 DexFile，描述要加载的类所在的 DEX 文件。
> dex_class_def: 类型为 ClassDef，描述要加载的类在 DEX 文件里面的信息。
> klass: 类型为 Class，描述加载完成的类。
> class_loader:  类型为 ClassLoader，描述所使用的类加载器。

LoadClass 的任务就是要用 dex_file、dex_class_def、class_loader 三个参数包含的相关信息设置到参数 klass 描述的 Class 对象去，以便可以得到一个完整的已加载类信息。

> 关键：
> 
>  - setClassLoader：将 class_loader 描述的 ClassLoader 设置到 klass 描述的 Class 对象中，即给每一个已加载的类关联一个类加载器。
>  - SetDexClassDefIndex：通过 DexFile 的成员函数 GetIndexForClassDef 获得正在加载的类在Dex文件中的索引号，并设置到 klass 中
> 
> FindOatClass：从相应的 OAT 文件中找到与正在加载的类对应的一个 OatClass 结构体 oat_class。这需要利用到上面提到的 DEX 类索引号，这是因为 DEX 类和 OAT 类根据索引号存在一一对应关系。

#### ClassLinker 类的成员函数 **LoadClassMembers**

```
void ClassLinker::LoadClassMembers(const DexFile& dex_file,
                                   const byte* class_data,
                                   Handle<mirror::Class> klass,
                                   mirror::ClassLoader* class_loader,
                                   const OatFile::OatClass* oat_class) {
  // Load fields.
  ClassDataItemIterator it(dex_file, class_data);
  Thread* self = Thread::Current();
  if (it.NumStaticFields() != 0) {
    mirror::ObjectArray<mirror::ArtField>* statics = AllocArtFieldArray(self, it.NumStaticFields());
    if (UNLIKELY(statics == nullptr)) {
      CHECK(self->IsExceptionPending());  // OOME.
      return;
    }
    klass->SetSFields(statics);
  }
  if (it.NumInstanceFields() != 0) {
    mirror::ObjectArray<mirror::ArtField>* fields =
        AllocArtFieldArray(self, it.NumInstanceFields());
    if (UNLIKELY(fields == nullptr)) {
      CHECK(self->IsExceptionPending());  // OOME.
      return;
    }
    klass->SetIFields(fields);
  }
  for (size_t i = 0; it.HasNextStaticField(); i++, it.Next()) {
    StackHandleScope<1> hs(self);
    Handle<mirror::ArtField> sfield(hs.NewHandle(AllocArtField(self)));
    if (UNLIKELY(sfield.Get() == nullptr)) {
      CHECK(self->IsExceptionPending());  // OOME.
      return;
    }
    klass->SetStaticField(i, sfield.Get());
    LoadField(dex_file, it, klass, sfield);
  }
  for (size_t i = 0; it.HasNextInstanceField(); i++, it.Next()) {
    StackHandleScope<1> hs(self);
    Handle<mirror::ArtField> ifield(hs.NewHandle(AllocArtField(self)));
    if (UNLIKELY(ifield.Get() == nullptr)) {
      CHECK(self->IsExceptionPending());  // OOME.
      return;
    }
    klass->SetInstanceField(i, ifield.Get());
    LoadField(dex_file, it, klass, ifield);
  }

  // Load methods.
  if (it.NumDirectMethods() != 0) {
    // TODO: append direct methods to class object
    mirror::ObjectArray<mirror::ArtMethod>* directs =
         AllocArtMethodArray(self, it.NumDirectMethods());
    if (UNLIKELY(directs == nullptr)) {
      CHECK(self->IsExceptionPending());  // OOME.
      return;
    }
    klass->SetDirectMethods(directs);
  }
  if (it.NumVirtualMethods() != 0) {
    // TODO: append direct methods to class object
    mirror::ObjectArray<mirror::ArtMethod>* virtuals =
        AllocArtMethodArray(self, it.NumVirtualMethods());
    if (UNLIKELY(virtuals == nullptr)) {
      CHECK(self->IsExceptionPending());  // OOME.
      return;
    }
    klass->SetVirtualMethods(virtuals);
  }
  size_t class_def_method_index = 0;
  uint32_t last_dex_method_index = DexFile::kDexNoIndex;
  size_t last_class_def_method_index = 0;
  for (size_t i = 0; it.HasNextDirectMethod(); i++, it.Next()) {
    StackHandleScope<1> hs(self);
    Handle<mirror::ArtMethod> method(hs.NewHandle(LoadMethod(self, dex_file, it, klass)));
    if (UNLIKELY(method.Get() == nullptr)) {
      CHECK(self->IsExceptionPending());  // OOME.
      return;
    }
    klass->SetDirectMethod(i, method.Get());
    LinkCode(method, oat_class, dex_file, it.GetMemberIndex(), class_def_method_index);
    uint32_t it_method_index = it.GetMemberIndex();
    if (last_dex_method_index == it_method_index) {
      // duplicate case
      method->SetMethodIndex(last_class_def_method_index);
    } else {
      method->SetMethodIndex(class_def_method_index);
      last_dex_method_index = it_method_index;
      last_class_def_method_index = class_def_method_index;
    }
    class_def_method_index++;
  }
  for (size_t i = 0; it.HasNextVirtualMethod(); i++, it.Next()) {
    StackHandleScope<1> hs(self);
    Handle<mirror::ArtMethod> method(hs.NewHandle(LoadMethod(self, dex_file, it, klass)));
    if (UNLIKELY(method.Get() == nullptr)) {
      CHECK(self->IsExceptionPending());  // OOME.
      return;
    }
    klass->SetVirtualMethod(i, method.Get());
    DCHECK_EQ(class_def_method_index, it.NumDirectMethods() + i);
    LinkCode(method, oat_class, dex_file, it.GetMemberIndex(), class_def_method_index);
    class_def_method_index++;
  }
  DCHECK(!it.HasNext());
}
```

从参数 dex_file 描述的 DEX 文件中获得正在加载的类的静态成员变量和实例成员变量个数，并且为每一个静态成员变量和实例成员变量都分配一个 ArtField 对象，接着通过 ClassLinker 类的成员函数 LoadField 对这些 ArtField 对象进行初始化。初始化得到的 ArtField 对象全部保存在 klass 描述的 Class 对象中。

参数 klass 描述的 Class 对象包含了一系列的 ArtField 对象和A rtMethod 对象，其中，ArtField 对象用来描述成员变量信息，而 ArtMethod 用来描述成员函数信息。

> 接下来继续分析 LinkCode 函数的实现，以便可以了解如何在一个 OAT 文件中找到一个 DEX 类方法的本地机器指令。

#### ClassLinker类的成员函数 **LinkCode**

```
void ClassLinker::LinkCode(Handle<mirror::ArtMethod> method, const OatFile::OatClass* oat_class,
                           const DexFile& dex_file, uint32_t dex_method_index,
                           uint32_t method_index) {
  if (Runtime::Current()->IsCompiler()) {
    // The following code only applies to a non-compiler runtime.
    return;
  }
  // Method shouldn't have already been linked.
  DCHECK(method->GetEntryPointFromQuickCompiledCode() == nullptr);
#if defined(ART_USE_PORTABLE_COMPILER)
  DCHECK(method->GetEntryPointFromPortableCompiledCode() == nullptr);
#endif
  if (oat_class != nullptr) {
    // Every kind of method should at least get an invoke stub from the oat_method.
    // non-abstract methods also get their code pointers.
    const OatFile::OatMethod oat_method = oat_class->GetOatMethod(method_index);
    oat_method.LinkMethod(method.Get());
  }

  // Install entry point from interpreter.
  bool enter_interpreter = NeedsInterpreter(method.Get(),
                                            method->GetEntryPointFromQuickCompiledCode(),
#if defined(ART_USE_PORTABLE_COMPILER)
                                            method->GetEntryPointFromPortableCompiledCode());
#else
                                            nullptr);
#endif
  if (enter_interpreter && !method->IsNative()) {
    method->SetEntryPointFromInterpreter(interpreter::artInterpreterToInterpreterBridge);
  } else {
    method->SetEntryPointFromInterpreter(artInterpreterToCompiledCodeBridge);
  }

  if (method->IsAbstract()) {
    method->SetEntryPointFromQuickCompiledCode(GetQuickToInterpreterBridge());
#if defined(ART_USE_PORTABLE_COMPILER)
    method->SetEntryPointFromPortableCompiledCode(GetPortableToInterpreterBridge());
#endif
    return;
  }

  bool have_portable_code = false;
  if (method->IsStatic() && !method->IsConstructor()) {
    // For static methods excluding the class initializer, install the trampoline.
    // It will be replaced by the proper entry point by ClassLinker::FixupStaticTrampolines
    // after initializing class (see ClassLinker::InitializeClass method).
    method->SetEntryPointFromQuickCompiledCode(GetQuickResolutionTrampoline());
#if defined(ART_USE_PORTABLE_COMPILER)
    method->SetEntryPointFromPortableCompiledCode(GetPortableResolutionTrampoline());
#endif
  } else if (enter_interpreter) {
    if (!method->IsNative()) {
      // Set entry point from compiled code if there's no code or in interpreter only mode.
      method->SetEntryPointFromQuickCompiledCode(GetQuickToInterpreterBridge());
#if defined(ART_USE_PORTABLE_COMPILER)
      method->SetEntryPointFromPortableCompiledCode(GetPortableToInterpreterBridge());
#endif
    } else {
      method->SetEntryPointFromQuickCompiledCode(GetQuickGenericJniTrampoline());
#if defined(ART_USE_PORTABLE_COMPILER)
      method->SetEntryPointFromPortableCompiledCode(GetPortableToQuickBridge());
#endif
    }
#if defined(ART_USE_PORTABLE_COMPILER)
  } else if (method->GetEntryPointFromPortableCompiledCode() != nullptr) {
    DCHECK(method->GetEntryPointFromQuickCompiledCode() == nullptr);
    have_portable_code = true;
    method->SetEntryPointFromQuickCompiledCode(GetQuickToPortableBridge());
#endif
  } else {
    DCHECK(method->GetEntryPointFromQuickCompiledCode() != nullptr);
#if defined(ART_USE_PORTABLE_COMPILER)
    method->SetEntryPointFromPortableCompiledCode(GetPortableToQuickBridge());
#endif
  }

  if (method->IsNative()) {
    // Unregistering restores the dlsym lookup stub.
    method->UnregisterNative(Thread::Current());

    if (enter_interpreter) {
      // We have a native method here without code. Then it should have either the GenericJni
      // trampoline as entrypoint (non-static), or the Resolution trampoline (static).
      DCHECK(method->GetEntryPointFromQuickCompiledCode() == GetQuickResolutionTrampoline()
          || method->GetEntryPointFromQuickCompiledCode() == GetQuickGenericJniTrampoline());
    }
  }

  // Allow instrumentation its chance to hijack code.
  Runtime* runtime = Runtime::Current();
  runtime->GetInstrumentation()->UpdateMethodsCode(method.Get(),
                                                   method->GetEntryPointFromQuickCompiledCode(),
#if defined(ART_USE_PORTABLE_COMPILER)
                                                   method->GetEntryPointFromPortableCompiledCode(),
#else
                                                   nullptr,
#endif
                                                   have_portable_code);
}
```

>  - 参数 method 表示要设置本地机器指令的类方法。
>  - 参数 oat_class 表示类方法 method 在 OAT 文件中对应的 OatClass 结构体。
>  - 参数 dex_file 表示在 dex 文件中对应的 DexFile。
>  - 参数 dex_method_index 表示 DexFile 中方法 method 的索引号。
>  - 参数 method_index 表示类方法 method 的索引号。

通过参数 method_index 描述的索引号可以在 oat_class 表示的OatClass结构体中找到一个 OatMethod 结构体 oat_method。这个 OatMethod 结构描述了类方法 method 的本地机器指令相关信息，通过调用它的成员函数 LinkMethod 可以将这些信息设置到参数 method 描述的 ArtMethod 对象中去。

#### OatMethod 的成员函数 **LinkMethod**

```
void OatFile::OatMethod::LinkMethod(mirror::ArtMethod* method) const {
  CHECK(method != NULL);
#if defined(ART_USE_PORTABLE_COMPILER)
  method->SetEntryPointFromPortableCompiledCode(GetPortableCode());
#endif
  method->SetEntryPointFromQuickCompiledCode(GetQuickCode());
}
```

通过 OatMethod 类的成员函数 GetPortableCode 和 GetQuickCode 获得 OatMethod 结构体中的 code_offset_ 字段，并且通过调用 ArtMethod 类的成员函数 SetEntryPointFromCompiledCode 设置到参数method描述的 ArtMethod 对象中去。OatMethod 结构体中的 code_offset_ 字段指向的是一个本地机器指令函数，这个本地机器指令函数正是通过翻译参数 method 描述的类方法的 DEX 字节码得到的。

##### OatMethod 的 **GetPortableCode** 函数

```
const void* GetPortableCode() const {
      // TODO: encode whether code is portable/quick in flags within OatMethod.
      if (kUsePortableCompiler) {
        return GetOatPointer<const void*>(code_offset_);
      } else {
        return nullptr;
      }
    }
```

##### OatMethod 的 **GetQuickCode** 函数

```
const void* GetQuickCode() const {
      if (kUsePortableCompiler) {
        return nullptr;
      } else {
        return GetOatPointer<const void*>(code_offset_);
      }
    }
```

#### ClassLinker类的全局函数 **NeedsInterpreter**

```
// Returns true if the method must run with interpreter, false otherwise.
static bool NeedsInterpreter(
    mirror::ArtMethod* method, const void* quick_code, const void* portable_code)
    SHARED_LOCKS_REQUIRED(Locks::mutator_lock_) {
  if ((quick_code == nullptr) && (portable_code == nullptr)) {
    // No code: need interpreter.
    // May return true for native code, in the case of generic JNI
    // DCHECK(!method->IsNative());
    return true;
  }
#ifdef ART_SEA_IR_MODE
  ScopedObjectAccess soa(Thread::Current());
  if (std::string::npos != PrettyMethod(method).find("fibonacci")) {
    LOG(INFO) << "Found " << PrettyMethod(method);
    return false;
  }
#endif
  // If interpreter mode is enabled, every method (except native and proxy) must
  // be run with interpreter.
  return Runtime::Current()->GetInstrumentation()->InterpretOnly() &&
         !method->IsNative() && !method->IsProxyMethod();
}
```

检查参数 method 描述的类方法是否需要通过解释器执行。

在以下两种情况下，一个类方法需要通过解释器来执行：
1. 没有对应的本地机器指令，即参数 quick_code 和 portable_code 的值等于 NULL。
2. ART 虚拟机运行在解释模式中，并且类方法不是 JNI 方法，并且也不是代理方法。

> 因为 JNI 方法是没有对应的 DEX 字节码的，因此即使 ART 虚拟机运行在解释模式中，JNI 方法也不能通过解释器来执行。至于代理方法，由于是动态生成的（没有对应的 DEX 字节码），因此即使 ART 虚拟机运行在解释模式中，它们也不通过解释器来执行。

调用 Runtime 类的静态成员函数 Current 获得的是描述 ART 运行时的一个 Runtime 对象。调用这个 Runtime 对象的成员函数 GetInstrumentation 获得的是一个 Instrumentation 对象。这个 Instrumentation 对象是用来调试 ART 运行时的，通过调用它的成员函数 InterpretOnly 可以知道 ART 虚拟机是否运行在解释模式中。

#### 回到 ClassLinker 类的成员函数 **LinkCode**

如果调用函数 NeedsInterpreter 得到的返回值 enter_interpreter 等于 true，而且不是 Native 方法，那么就意味着参数 method 描述的类方法需要通过解释器来执行，这时候就将函数 artInterpreterToInterpreterBridge 设置为解释器执行该类方法的入口点。否则的话，就将另外一个函数 artInterpreterToCompiledCodeBridge 设置为解释器执行该类方法的入口点。

> 为什么我们需要为类方法设置解释器入口点呢？根据前面的分析可以知道，在 ART 虚拟机中，并不是所有的类方法都是有对应的本地机器指令的，并且即使一个类方法有对应的本地机器指令，当ART虚拟机以解释模式运行时，它也需要通过解释器来执行。当以解释器执行的类方法在执行的过程中调用了其它的类方法时，解释器就需要进一步知道被调用的类方法是应用以解释方式执行，还是本地机器指令方法执行。为了能够进行统一处理，就给每一个类方法都设置一个解释器入口点。需要通过解释执行的类方法的解释器入口点函数是 artInterpreterToInterpreterBridge，它会继续通过解释器来执行该类方法。需要通过本地机器指令执行的类方法的解释器入口点函数是 artInterpreterToCompiledCodeBridge，它会间接地调用该类方法的本地机器指令。

判断 method 是否是一个抽象方法。抽象方法声明类中是没有实现的，必须要由子类实现。因此抽象方法在声明类中是没有对应的本地机器指令的，它们必须要通过解释器来执行。不过，为了能够进行统一处理，我们仍然假装抽象方法有对应的本地机器指令函数，只不过这个本地机器指令函数被设置为 GetQuickToInterpreterBridge。当函数 GetQuickToInterpreterBridge，就会自动进入到解释器中去。

当 method 是一个非类静态初始化函数(class initializer)的静态方法时，我们不能直接执行翻译其 DEX 字节码得到的本地机器指令。这是因为类静态方法可以在不创建类对象的前提下执行。这意味着一个类静态方法在执行的时候，对应的类可能还没有初始化好。这时候我们就需要先将对应的类初始化好，再执行相应的静态方法。为了能够做到这一点。我们就先调用 GetResolutionTrampoline 函数得到一个 Tampoline 函数，接着将这个 Trampoline 函数作为静态方法的本地机器指令。这样如果类静态方法在对应的类初始化前被调用，就会触发上述的 Trampoline 函数被执行。而当上述 Trampoline 函数执行时，它们先初始化好对应的类，再调用原来的类静态方法对应的本地机器指令。按照代码中的注释，当一个类初始化完成之后，就可以调用函数 ClassLinker::FixupStaticTrampolines 来修复该类的静态成员函数的本地机器指令，也是通过翻译 DEX 字节码得到的本地机器指令。这里需要注意的是，为什么类静态初始化函数不需要按照其它的类静态方法一样设置 Tampoline 函数呢？这是因为类静态初始化函数是一定保证是在类初始化过程中执行的。

当 method 需要通过解释器执行时，那么当该类方法执行时，就不能执行它的本地机器指令，因此我们就先调用 GetCompiledCodeToInterpreterBridge 函数获得一个桥接函数，并且将这个桥接函数假装为类方法的本地机器指令。一旦该桥接函数被执行，它就会入到解释器去执行类方法。通过这种方式，我们就可以以统一的方法来调用解释执行和本地机器指令执行的类方法。

判断 method 是否是一个 JNI 方法。如果是的话，那么就调用 ArtMethod 类的成员函数 UnregisterNative 来初始化它的 JNI 方法调用接口。

#### ArtMethod 类的成员函数 **UnregisterNative**

```
void ArtMethod::UnregisterNative(Thread* self) {
  CHECK(IsNative() && !IsFastNative()) << PrettyMethod(this);
  // restore stub to lookup native pointer via dlsym
  RegisterNative(self, GetJniDlsymLookupStub(), false);
}
```

UnregisterNative 实际上就是将一个 JNI 方法的初始化入口设置为通过调用函数 GetJniDlsymLookupStub 获得的一个 Stub。这个 Stub 的作用是，当一个 JNI 方法被调用时，如果还没有显示地注册有 Native 函数，那么它就会自动从已加载的 SO 文件查找是否存在一个对应的 Native 函数。如果存在的话，就将它注册为 JNI 方法的 Native 函数，并且执行它。这就是隐式的 JNI 方法注册。
 
#### 回到 ClassLinker 类的成员函数 **LinkCode**

最后调用 Instrumentation 类的成员函数 UpdateMethodsCode 检查是否要进一步修改参数 method 描述的类方法的本地机器指令入口。

```
void Instrumentation::UpdateMethodsCode(mirror::ArtMethod* method, const void* quick_code,
                                        const void* portable_code, bool have_portable_code) {
  const void* new_portable_code;
  const void* new_quick_code;
  bool new_have_portable_code;
  if (LIKELY(!instrumentation_stubs_installed_)) {
    new_portable_code = portable_code;
    new_quick_code = quick_code;
    new_have_portable_code = have_portable_code;
  } else {
    if ((interpreter_stubs_installed_ || IsDeoptimized(method)) && !method->IsNative()) {
#if defined(ART_USE_PORTABLE_COMPILER)
      new_portable_code = GetPortableToInterpreterBridge();
#else
      new_portable_code = portable_code;
#endif
      new_quick_code = GetQuickToInterpreterBridge();
      new_have_portable_code = false;
    } else {
      ClassLinker* class_linker = Runtime::Current()->GetClassLinker();
      if (quick_code == class_linker->GetQuickResolutionTrampoline() ||
          quick_code == class_linker->GetQuickToInterpreterBridgeTrampoline() ||
          quick_code == GetQuickToInterpreterBridge()) {
#if defined(ART_USE_PORTABLE_COMPILER)
        DCHECK((portable_code == class_linker->GetPortableResolutionTrampoline()) ||
               (portable_code == GetPortableToInterpreterBridge()));
#endif
        new_portable_code = portable_code;
        new_quick_code = quick_code;
        new_have_portable_code = have_portable_code;
      } else if (entry_exit_stubs_installed_) {
        new_quick_code = GetQuickInstrumentationEntryPoint();
#if defined(ART_USE_PORTABLE_COMPILER)
        new_portable_code = GetPortableToInterpreterBridge();
#else
        new_portable_code = portable_code;
#endif
        new_have_portable_code = false;
      } else {
        new_portable_code = portable_code;
        new_quick_code = quick_code;
        new_have_portable_code = have_portable_code;
      }
    }
  }
  UpdateEntrypoints(method, new_quick_code, new_portable_code, new_have_portable_code);
}
```

Instrumentation 类是用来调用 ART 运行时的。例如，当我们需要监控类方法的调用时，就可以往 Instrumentation 注册一些 Listener。这样当类方法调用时，这些注册的 Listener 就会得到回调。当 Instrumentation 注册有相应的 Listener 时，它的成员变量 instrumentation_stubs_installed_ 的值就会等于 true。

> **总结**：
通过上述源码跟踪分析，一个类的加载过程完成了。加载完成后得到的是一个 Class 对象。这个 Class 对象关联有一系列的 ArtField 对象和 ArtMethod 对象。其中，ArtField 对象描述的是成员变量，而 ArtMethod 对象描述的是成员函数。对于每一个 ArtMethod 对象，它都有一个解释器入口点和一个本地机器指令入口点。这样，无论一个类方法是通过解释器执行，还是直接以本地机器指令执行，我们都可以以统一的方式来进行调用。同时，理解了上述的类加载过程后，我们就可以知道，我们在 Native 层通过 JNI 接口 FindClass 查找或者加载类时，得到的一个不透明的 jclass 值，实际上指向的是一个 Class 对象。

### 类方法查找流程

>  1. GetStaticMethodID (/art/runtime/jni_internal.cc#943)
>  2. FindMethodID (/art/runtime/jni_internal.cc#140)

#### JNI 类的静态成员函数 **FindClass**

```
static jmethodID GetStaticMethodID(JNIEnv* env, jclass java_class, const char* name,
                                     const char* sig) {
    CHECK_NON_NULL_ARGUMENT(java_class);
    CHECK_NON_NULL_ARGUMENT(name);
    CHECK_NON_NULL_ARGUMENT(sig);
    ScopedObjectAccess soa(env);
    return FindMethodID(soa, java_class, name, sig, true);
}
```

参数 name 和 sig 描述的分别是要查找的类方法的名称和签名，而参数 java_class 是对应的类。参数 java_class 的类型是 jclass，从前面类加载过程的分析可以知道，它实际上指向的是一个 Class 对象。

#### JNI 类的静态成员函数 **FindMethodID**

```
static jmethodID FindMethodID(ScopedObjectAccess& soa, jclass jni_class,
                              const char* name, const char* sig, bool is_static)
    SHARED_LOCKS_REQUIRED(Locks::mutator_lock_) {
  mirror::Class* c = EnsureInitialized(soa.Self(), soa.Decode<mirror::Class*>(jni_class));
  if (c == nullptr) {
    return nullptr;
  }
  mirror::ArtMethod* method = nullptr;
  if (is_static) {
    method = c->FindDirectMethod(name, sig);
  } else if (c->IsInterface()) {
    method = c->FindInterfaceMethod(name, sig);
  } else {
    method = c->FindVirtualMethod(name, sig);
    if (method == nullptr) {
      // No virtual method matching the signature.  Search declared
      // private methods and constructors.
      method = c->FindDeclaredDirectMethod(name, sig);
    }
  }
  if (method == nullptr || method->IsStatic() != is_static) {
    ThrowNoSuchMethodError(soa, c, name, sig, is_static ? "static" : "non-static");
    return nullptr;
  }
  return soa.EncodeMethod(method);
}
```
执行过程:
1. 将参数 jni_class 的值转换为一个 Class 指针 c，因此就可以得到一个 Class 对象，并且通过 ClassLinker 类的成员函数 EnsureInitialized 确保该 Class 对象描述的类已经初始化。
2. Class 对象 c 描述的类在加载的过程中，经过解析已经关联上一系列的成员函数。这些成员函数可以分为两类：Direct 和 Virtual。Direct 类的成员函数包括所有的静态成员函数、私有成员函数和构造函数，而 Virtual 则包括所有的虚成员函数。
3. 经过前面的查找过程，如果都不能在 Class 对象 c 描述的类中找到与参数 name 和 sig 对应的成员函数，那么就抛出一个 NoSuchMethodError 异常。否则的话，就将查找得到的 ArtMethod 对象封装成一个 jmethodID 值返回给调用者。

我们通过调用 JNI 接口 GetStaticMethodID 获得的不透明 jmethodID 值指向的实际上是一个 ArtMethod 对象。
当我们获得了一个 ArtMethod 对象之后，就可以轻松地得到它的本地机器指令入口，进而对它进行执行。

> **总结**：
当我们把旧方法(ArtMethod)的所有成员字段都替换为新方法(ArtMethod)的成员字段后，执行时所有的数据就可以保持和新方法的数据一致。这样在所有执行到旧方法的地方，会获取新方法的执行入口、所属类型、方法索引号、以及所属 dex 信息，然后像调用旧方法一样，执行新方法的逻辑。