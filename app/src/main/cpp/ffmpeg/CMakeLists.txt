#----------测试代码------------
set(var "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}")
set(ffres "${CMAKE_SOURCE_DIR}/ffmpeg/lib_res")
message("测试路径 : var = ${var}")
message("测试路径 : var = ${ffres}")
#----------测试代码------------
#------------- ffmpeg-----------------
include_directories(lib_res)
#-L 库的查找路径  -D 定义宏
#目录是全是.c文件需要使用cmake_c_flags ,如果目录存在一个.cpp那就得用cmake_cxx_flages
#链接jniLibs目录下的ffmpeg的静态库,链接x264静态库
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/ffmpeg/${CMAKE_ANDROID_ARCH_ABI}")

get_property(dirs DIRECTORY ${CMAKE_SOURCE_DIR} PROPERTY INCLUDE_DIRECTORIES)
message(">>> include_dirs=${dirs}") #打印一下目录情况

file(GLOB ffmpeg_res_file *.cpp)
file(GLOB ffmpeg_res_channel channel/*.cpp)
file(GLOB ffmpeg_res_callback java_callback/*.cpp)

foreach (file IN LISTS ffmpeg_res_file)
    message("路径: ${file}")
endforeach (file)

add_library(
        ffmpeg_lib
        SHARED
        ${ffmpeg_res_file}
        ${ffmpeg_res_channel}
        ${ffmpeg_res_callback}
)

target_link_libraries(
        ffmpeg_lib
        #静态库引入需要按照引用顺序引入,为了避免不必要的错误可以将一系列的静态库当做一个组引入
        -Wl,--start-group
        avcodec avfilter avformat avutil swresample swscale
        -Wl,--end-group
        log
        z
        rtmp
        android
        OpenSLES
)
#------------- ffmpeg-----------------
