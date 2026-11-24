cmake_minimum_required(VERSION 3.22.1)
project(sdai_mnn_runtime LANGUAGES C CXX)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)
# Build IDs hash debug inputs before stripping and can retain build-path variance.
# Apply before adding any dependency targets, as recommended by F-Droid.
add_link_options("LINKER:--build-id=none")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384")

# Keep source locations and debug/build-id inputs independent of checkout paths.
# Apply before adding MNN so its static dependencies receive the same mapping.
get_filename_component(SDAI_MNN_REAL_SOURCE_DIR "${CMAKE_SOURCE_DIR}" REALPATH)
get_filename_component(SDAI_MNN_REAL_BINARY_DIR "${CMAKE_BINARY_DIR}" REALPATH)
get_filename_component(SDAI_MNN_REAL_NDK_DIR "${CMAKE_ANDROID_NDK}" REALPATH)
add_compile_options(
    "-ffile-prefix-map=${CMAKE_SOURCE_DIR}=."
    "-ffile-prefix-map=${CMAKE_BINARY_DIR}=."
    "-ffile-prefix-map=${CMAKE_ANDROID_NDK}=/android-ndk"
    "-ffile-prefix-map=${SDAI_MNN_REAL_SOURCE_DIR}=."
    "-ffile-prefix-map=${SDAI_MNN_REAL_BINARY_DIR}=."
    "-ffile-prefix-map=${SDAI_MNN_REAL_NDK_DIR}=/android-ndk"
)

include(FetchContent)
get_filename_component(SDAI_MNN_ADAPTER "${CMAKE_CURRENT_LIST_DIR}/../../../swift/Sources/CMnnBridge/Runtime" ABSOLUTE)
include_directories("${SDAI_MNN_ADAPTER}")
FetchContent_Declare(mnn
    URL https://github.com/alibaba/MNN/archive/7e3c6fb5225d3ccf22807ee641332669df34650d.tar.gz
    URL_HASH SHA256=01dcdce39072b70188079a1dca0600e782c8dd0fcf2ad77f610bab264166a0cf
)
FetchContent_GetProperties(mnn)
if(NOT mnn_POPULATED)
    FetchContent_Populate(mnn)
endif()

# Apply the narrow SD 1.5 adapter to a build-local copy, retaining the immutable source.
set(SDAI_MNN_SOURCE "${CMAKE_CURRENT_BINARY_DIR}/mnn-adapted")
file(SHA256 "${SDAI_MNN_ADAPTER}/patches/sdai-sd15.patch" SDAI_MNN_PATCH_HASH)
set_property(DIRECTORY APPEND PROPERTY CMAKE_CONFIGURE_DEPENDS "${SDAI_MNN_ADAPTER}/patches/sdai-sd15.patch")
if(NOT EXISTS "${SDAI_MNN_SOURCE}/.sdai-patched-${SDAI_MNN_PATCH_HASH}")
    file(REMOVE_RECURSE "${SDAI_MNN_SOURCE}")
    file(COPY "${mnn_SOURCE_DIR}/" DESTINATION "${SDAI_MNN_SOURCE}")
    find_program(PATCH_EXECUTABLE patch REQUIRED)
    execute_process(COMMAND "${PATCH_EXECUTABLE}" -p1 -i "${SDAI_MNN_ADAPTER}/patches/sdai-sd15.patch"
        WORKING_DIRECTORY "${SDAI_MNN_SOURCE}" COMMAND_ERROR_IS_FATAL ANY)
    file(WRITE "${SDAI_MNN_SOURCE}/.sdai-patched-${SDAI_MNN_PATCH_HASH}" "3.6.2")
endif()

set(MNN_BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(MNN_BUILD_FOR_ANDROID_COMMAND ON CACHE BOOL "" FORCE)
set(MNN_SEP_BUILD OFF CACHE BOOL "" FORCE)
set(MNN_BUILD_LLM ON CACHE BOOL "" FORCE)
set(MNN_BUILD_DIFFUSION ON CACHE BOOL "" FORCE)
set(MNN_BUILD_OPENCV ON CACHE BOOL "" FORCE)
set(MNN_IMGCODECS ON CACHE BOOL "" FORCE)
set(MNN_LOW_MEMORY ON CACHE BOOL "" FORCE)
set(MNN_OPENCL ON CACHE BOOL "" FORCE)
set(MNN_OPENMP OFF CACHE BOOL "" FORCE)
set(MNN_BUILD_DEMO OFF CACHE BOOL "" FORCE)
set(MNN_BUILD_TEST OFF CACHE BOOL "" FORCE)
set(MNN_BUILD_CONVERTER OFF CACHE BOOL "" FORCE)
set(MNN_BUILD_LLM_OMNI OFF CACHE BOOL "" FORCE)
set(MNN_LLM_BUILD_DEMO OFF CACHE BOOL "" FORCE)
set(LLM_SUPPORT_HTTP_RESOURCE OFF CACHE BOOL "" FORCE)
# Keep reserved, auto-removed CPU mmap backing strict inside this runtime only.
add_compile_definitions(SDAI_MNN_PRIVATE_MMAP=1)
# Allocation failures must unwind all dependency frames to the JNI error boundary.
add_compile_options(-fexceptions)
add_subdirectory("${SDAI_MNN_SOURCE}" "${CMAKE_CURRENT_BINARY_DIR}/mnn-build" EXCLUDE_FROM_ALL)
target_compile_options(diffusion PRIVATE -fexceptions)
target_sources(diffusion PRIVATE "${SDAI_MNN_ADAPTER}/sdai_unet_graph.cpp")
target_include_directories(diffusion PRIVATE "${SDAI_MNN_SOURCE}/tools/cv/include"
    "${SDAI_MNN_ADAPTER}" "${SDAI_MNN_SOURCE}/schema/current"
    "${SDAI_MNN_SOURCE}/3rd_party/flatbuffers/include")

add_library(sdai_mnn SHARED mnn_jni.cpp mnn_upscaler_jni.cpp)
target_include_directories(sdai_mnn PRIVATE "${SDAI_MNN_SOURCE}/include"
    "${SDAI_MNN_ADAPTER}"
    "${SDAI_MNN_SOURCE}/transformers/diffusion/engine/include")
target_link_libraries(sdai_mnn PRIVATE MNN android log)
target_compile_options(sdai_mnn PRIVATE -Wall -Wextra -Wno-unused-parameter)
