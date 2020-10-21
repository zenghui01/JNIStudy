#------------ QQ变声------------------
#链接头文件
include_directories(fmod_inc)
#链接动态库
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}")
add_library(
        fmod_lib
        SHARED
        fmod_lib.cpp
)

target_link_libraries(
        fmod_lib
        fmod
        fmodL
        log
)
#------------ QQ变声------------------
