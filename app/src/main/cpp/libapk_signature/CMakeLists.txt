#----------签名测试模块---------------
include_directories(libapk_signature)

add_library(
        apkSign-lib
        SHARED
        apk_signature_lib.cpp
)

target_link_libraries(
        apkSign-lib
        log)
#----------签名测试模块---------------
