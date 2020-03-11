cmake_minimum_required(VERSION 3.4.1)

#----------热修复模块---------------
include_directories(./hotfix)

add_library(
        native-lib
        SHARED
        native-lib.cpp
)

target_link_libraries(
        native-lib
        log)
#----------热修复模块---------------

#-------- gif模块--------------
#链接头文件,当include的时候能够自动从该目录中寻找所需头文件
include_directories(./gif_file)
#链接.c资源文件
aux_source_directory(./gif_file gif_files)

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

#-------- 增量更新模块 --------------
#链接头文件,当include的时候能够自动从该目录中寻找所需头文件
include_directories(./bzip2)
#链接.c资源文件
aux_source_directory(./bzip2 bzip_srcs)

add_library(
        bzip_lib
        SHARED
        bzip_lib.cpp ${bzip_srcs}
)

target_link_libraries(
        bzip_lib
        log)

#-------- 增量更新模块 --------------