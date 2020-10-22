#------------- rtmp-----------------
include_directories(rtmp_push_lib.cpp)
#添加rtmp库
add_subdirectory(librtmp)
#添加x264头文件
include_directories(x264_res)

set(CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/librtmppush/${CMAKE_ANDROID_ARCH_ABI}")

add_library(
        rtmp_push
        SHARED
        rtmp_push_lib.cpp
)

target_link_libraries(
        rtmp_push
        rtmp
        x264
)
#------------- rtmp -----------------
