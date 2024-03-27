package com.testndk.jnistudy.ui.activity

import android.os.Environment
import android.view.View
import com.testndk.jnistudy.R
import com.testndk.jnistudy.ui.BaseActivity
import com.testndk.jnistudy.ui.andfix.DexManager
import com.testndk.jnistudy.ui.andfix.error.Calculator
import com.testndk.jnistudy.utils.toast
import io.reactivex.disposables.CompositeDisposable
import java.io.File

/**
* 如何生成dex文件
* dx --dex --output [输出文件命名] [文件路径]
*
* adb push [指定文件] [手机文件目录]
*手机文件目录
* adb devices 查看当前连接设备
*
* adb install [安装文件]
*/
class MainActivity : BaseActivity() {
   override fun initView() {
       calculator = Calculator()
//        val permission = RxPermissions(this)
//        val subscribe = permission.request(Manifest.permission_group.STORAGE)
//            .subscribe {
//                if (it) {
//
//                }
//            }
//        mDisable.add(subscribe)
   }

   override fun initLayout(): Int {
       return R.layout.activity_main;
   }

   companion object {
       init {
           System.loadLibrary("hotfix-lib")
       }
   }

   lateinit var calculator: Calculator
   val mDisable = CompositeDisposable()


   fun onClickBug(view: View) {
       calculator.calculator(this)
   }

   fun onClickFix(view: View) {
       DexManager.getInstance().setContext(this)
       val file = File(Environment.getExternalStorageDirectory().path, "out.dex");
       if (file.exists()) {
           DexManager.getInstance()
               .loadDex(file)
       } else {
           toast("补丁包不存在")
       }
   }
}
