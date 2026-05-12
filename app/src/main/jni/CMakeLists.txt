cmake_minimum_required(VERSION 3.22.1)
project("scrollbot")

set(LLAMA_DIR ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp)

set(LLAMA_ANDROID ON CACHE BOOL "" FORCE)
set(LLAMA_NNAPI ON CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)

add_subdirectory(${LLAMA_DIR} llama_build)

# Glob all llava cpp sources — handles any llama.cpp version
file(GLOB LLAVA_SRCS
    "${LLAMA_DIR}/examples/llava/*.cpp"
    "${LLAMA_DIR}/tools/llava/*.cpp"
)

if(LLAVA_SRCS)
    add_library(llava STATIC ${LLAVA_SRCS})
    target_include_directories(llava PRIVATE
        ${LLAMA_DIR}
        ${LLAMA_DIR}/include
        ${LLAMA_DIR}/ggml/include
        ${LLAMA_DIR}/common
        ${LLAMA_DIR}/examples/llava
        ${LLAMA_DIR}/tools/llava
    )
    target_link_libraries(llava llama)
    set(LLAVA_LINK llava)
else()
    message(WARNING "llava sources not found — vision features disabled")
    set(LLAVA_LINK "")
endif()

add_library(
    moondream_jni
    SHARED
    moondream_jni.cpp
)

target_include_directories(moondream_jni PRIVATE
    ${LLAMA_DIR}
    ${LLAMA_DIR}/include
    ${LLAMA_DIR}/ggml/include
    ${LLAMA_DIR}/common
    ${LLAMA_DIR}/examples/llava
    ${LLAMA_DIR}/tools/llava
)

target_link_libraries(moondream_jni
    ${LLAVA_LINK}
    llama
    log
    android
)
