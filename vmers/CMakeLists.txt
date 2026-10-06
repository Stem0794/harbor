cmake_minimum_required(VERSION 3.10)
project(VmersCoreEngine CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(SOURCES
    main.cpp
    property_service.cpp
    vmlink_loader.cpp
)

add_executable(vmers_engine ${SOURCES})
target_link_libraries(vmers_engine pthread)
