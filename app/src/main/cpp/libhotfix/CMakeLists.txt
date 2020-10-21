#----------热修复模块---------------
include_directories(libhotfix)

add_library(
        hotfix-lib
        SHARED
        hotfix_lib.cpp
)

target_link_libraries(
        hotfix-lib
        log)
#----------热修复模块---------------