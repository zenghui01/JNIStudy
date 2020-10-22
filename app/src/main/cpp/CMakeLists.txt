cmake_minimum_required(VERSION 3.4.1)

#----------测试代码------------
set(var "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}")

message("测试路径 : var = ${var}")
#----------测试代码------------
add_subdirectory(libapk_signature)
add_subdirectory(libgif)
add_subdirectory(libfmod)
add_subdirectory(libhotfix)
add_subdirectory(libbzip)
add_subdirectory(ffmpeg)
add_subdirectory(librtmppush)
