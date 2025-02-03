cmake_minimum_required(VERSION 3.10.2)
project("otp")

# Thiết lập thư mục chứa các file header của OpenSSL
include_directories(${CMAKE_SOURCE_DIR}/src/main/jni/include)

# Thêm thư mục chứa các thư viện `.so`
set(OpenSSL_LIB_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs)

# Định nghĩa target otp trước khi liên kết các thư viện
add_library(otp SHARED src/main/cpp/OtpJNI.cpp)

# Liên kết thư viện OpenSSL ssl và crypto
add_library(ssl SHARED IMPORTED)
set_target_properties(ssl PROPERTIES IMPORTED_LOCATION
        ${OpenSSL_LIB_DIR}/${ANDROID_ABI}/libssl.so)

add_library(crypto SHARED IMPORTED)
set_target_properties(crypto PROPERTIES IMPORTED_LOCATION
        ${OpenSSL_LIB_DIR}/${ANDROID_ABI}/libcrypto.so)

# Liên kết các thư viện OpenSSL vào target otp
target_link_libraries(otp ssl crypto)