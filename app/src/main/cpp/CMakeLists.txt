cmake_minimum_required(VERSION 3.4.1)


#gif模块
aux_source_directory(./gif gif_files)

add_library(
        native-lib
        SHARED
        native-lib.cpp
)

add_library(
        gif-lib
        SHARED
        gif_lib.cpp ${gif_files}
)

target_link_libraries(
        native-lib
        log)

target_link_libraries(
        gif-lib
        jnigraphics
        log)

