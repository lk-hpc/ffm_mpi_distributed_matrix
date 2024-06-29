cmake_minimum_required(VERSION 3.10)
project(SimpleLibrary)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED True)

find_package(MPI REQUIRED)

if(MPI_FOUND)
    include_directories(SYSTEM ${MPI_INCLUDE_PATH})
    message("MPI include path: ${MPI_INCLUDE_PATH}")
else(MPI_FOUND)
    message(SEND_ERROR "Application requires MPI")
    return()
endif(MPI_FOUND)

# Include directories
include_directories(${PROJECT_SOURCE_DIR}/src)

# Add all source files to SOURCES variable
file(GLOB SOURCES "src/*.cpp")

add_library(SimpleLib SHARED ${SOURCES})
target_link_libraries(SimpleLib ${MPI_LIBRARIES})