cmake_minimum_required(VERSION 3.10.2)
project("otp")

# Thư mục header của OpenSSL
include_directories(${CMAKE_SOURCE_DIR}/src/main/jni/include)

# Thư mục chứa các thư viện OpenSSL .so
set(OpenSSL_LIB_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs)

# Tạo thư viện native otp từ source C++
add_library(otp SHARED src/main/cpp/OtpJNI.cpp)

# Import thư viện ssl
add_library(ssl SHARED IMPORTED)
set_target_properties(ssl PROPERTIES IMPORTED_LOCATION ${OpenSSL_LIB_DIR}/${ANDROID_ABI}/libssl.so)

# Import thư viện crypto
add_library(crypto SHARED IMPORTED)
set_target_properties(crypto PROPERTIES IMPORTED_LOCATION ${OpenSSL_LIB_DIR}/${ANDROID_ABI}/libcrypto.so)

# Liên kết thư viện OpenSSL và log vào otp
target_link_libraries(otp ssl crypto log)

# Đảm bảo hỗ trợ page size 16K (Android 15+ yêu cầu)
target_link_options(otp PRIVATE -Wl,-z,max-page-size=16384)