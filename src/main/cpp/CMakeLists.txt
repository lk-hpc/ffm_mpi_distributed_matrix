cmake_minimum_required(VERSION 3.10)
project(SimpleLibrary)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED True)

find_package(MPI REQUIRED)

if(MPI_FOUND)
    include_directories(SYSTEM ${MPI_INCLUDE_PATH})
else(MPI_FOUND)
    message(SEND_ERROR "Application requires MPI")
    return()
endif(MPI_FOUND)

# Helper function to create a new target
function(create_target target_name source_files)
    add_executable(${target_name} ${source_files})
    target_link_libraries(${target_name} utils)
    target_link_libraries(${target_name} ${MPI_LIBRARIES})

    if(MPI_COMPILE_FLAGS)
        set_target_properties(${target_name} PROPERTIES
                COMPILE_FLAGS "${MPI_COMPILE_FLAGS}")
    endif()

    if(MPI_LINK_FLAGS)
        set_target_properties(${target_name} PROPERTIES
                LINK_FLAGS "${MPI_LINK_FLAGS}")
    endif()
endfunction()

# Include directories
include_directories(${PROJECT_SOURCE_DIR}/src)

# Add all source files to SOURCES variable
file(GLOB SOURCES "src/*.cpp")

add_library(SimpleLib SHARED ${SOURCES})