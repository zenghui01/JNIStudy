#------------- rtmp-----------------
#-L 库的查找路径  -D 定义宏
#目录是全是.c文件需要使用cmake_c_flags ,如果目录存在一个.cpp那就得用cmake_cxx_flages
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")

file(GLOB res_file *.c)

add_library(
        rtmp
        STATIC
        ${res_file}
)
#------------- rtmp -----------------
