#-------- gif模块--------------
#链接头文件,当include的时候能够自动从该目录中寻找所需头文件
#include_directories(./)
#链接动态库
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}")
#链接.c资源文件
aux_source_directory(./ gif_files)

add_library(
        gif-lib
        SHARED
        gif_lib.cpp ${gif_files}
)

target_link_libraries(
        gif-lib
        jnigraphics
        log)
#-------- gif模块--------------
