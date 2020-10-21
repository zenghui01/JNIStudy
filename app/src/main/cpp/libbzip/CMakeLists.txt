#-------- 增量更新模块 --------------

#链接头文件,当include的时候能够自动从该目录中寻找所需头文件
include_directories(./)
#链接.c资源文件
aux_source_directory(./ bzip_srcs)

add_library(
        bzip_lib
        SHARED
        bzip_lib.cpp ${bzip_srcs}
)

#-------- 增量更新模块 --------------