#include <openssl/aes.h>
#include <openssl/rand.h>
#include <openssl/evp.h>
#include <openssl/sha.h>
#include <sstream>
#include <iostream>
#include <cstring>
#include <iomanip>
#include <jni.h>
#include <jni.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_presentation_main_setting_security_manager_OTPUtils_encryptOTP(JNIEnv *env,
                                                                                jobject thiz,
                                                                                jstring otp,
                                                                                jstring secret_key) {
    // Lấy giá trị OTP và secret key từ Java
    const char* otpStr = env->GetStringUTFChars(otp, nullptr);
    const char* secret = env->GetStringUTFChars(secret_key, nullptr);

    // Chuyển OTP và secret thành mảng byte
    std::string otpInput(otpStr);
    std::string key(secret);

    // Tạo khóa AES từ secretKey (có thể sử dụng SHA256 để đảm bảo khóa có độ dài 256 bit)
    unsigned char aesKey[32];  // 256-bit key for AES-256
    SHA256_CTX sha256Ctx;
    SHA256_Init(&sha256Ctx);
    SHA256_Update(&sha256Ctx, key.c_str(), key.size());
    SHA256_Final(aesKey, &sha256Ctx);

    // Mã hóa OTP sử dụng AES (chế độ CBC)
    AES_KEY encryptKey;
    unsigned char iv[AES_BLOCK_SIZE];
    unsigned char encrypted[128];

    // Khởi tạo IV (Initialization Vector) ngẫu nhiên
    if (!RAND_bytes(iv, AES_BLOCK_SIZE)) {
        std::cerr << "Error generating IV." << std::endl;
        return nullptr;
    }

    // Thiết lập khóa AES cho mã hóa
    if (AES_set_encrypt_key(aesKey, 256, &encryptKey) < 0) {
        std::cerr << "Error setting AES encryption key." << std::endl;
        return nullptr;
    }

    // Padding dữ liệu OTP để phù hợp với kích thước block của AES
    int dataLen = otpInput.size();
    int paddedLen = ((dataLen / AES_BLOCK_SIZE) + 1) * AES_BLOCK_SIZE;
    unsigned char paddedData[paddedLen];
    std::memcpy(paddedData, otpInput.c_str(), dataLen);
    std::memset(paddedData + dataLen, 0, paddedLen - dataLen); // Padding bằng 0

    // Mã hóa OTP
    AES_cbc_encrypt(paddedData, encrypted, paddedLen, &encryptKey, iv, AES_ENCRYPT);

    // Chuyển kết quả mã hóa thành chuỗi hex để trả về
    std::ostringstream hexStream;
    for (int i = 0; i < paddedLen; i++) {
        hexStream << std::hex << std::setw(2) << std::setfill('0') << (int)encrypted[i];
    }

    std::string encryptedHex = hexStream.str();
    return env->NewStringUTF(encryptedHex.c_str());
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_presentation_main_setting_security_manager_OTPUtils_decryptOTP(JNIEnv *env, jobject /* this */, jstring encryptedOTP, jstring secretKey) {
    // Lấy giá trị encrypted OTP và secret key từ Java
    const char* encryptedOTPStr = env->GetStringUTFChars(encryptedOTP, nullptr);
    const char* secret = env->GetStringUTFChars(secretKey, nullptr);

    // Chuyển chuỗi hex của OTP mã hóa thành mảng byte
    int encryptedOTPLength = strlen(encryptedOTPStr) / 2;
    unsigned char encryptedData[encryptedOTPLength];
    for (int i = 0; i < encryptedOTPLength; i++) {
        sscanf(&encryptedOTPStr[i * 2], "%2hhx", &encryptedData[i]);
    }

    // Tạo khóa AES từ secretKey (có thể sử dụng SHA256 để đảm bảo khóa có độ dài 256 bit)
    unsigned char aesKey[32];  // 256-bit key for AES-256
    SHA256_CTX sha256Ctx;
    SHA256_Init(&sha256Ctx);
    SHA256_Update(&sha256Ctx, secret, strlen(secret));
    SHA256_Final(aesKey, &sha256Ctx);

    // Tách IV từ dữ liệu mã hóa (IV thường được lưu ở phần đầu của chuỗi mã hóa)
    unsigned char iv[AES_BLOCK_SIZE];
    std::memcpy(iv, encryptedData, AES_BLOCK_SIZE);  // IV là 16 byte đầu tiên của dữ liệu mã hóa

    // Thiết lập khóa AES cho giải mã
    AES_KEY decryptKey;
    if (AES_set_decrypt_key(aesKey, 256, &decryptKey) < 0) {
        std::cerr << "Error setting AES decryption key." << std::endl;
        return nullptr;
    }

    // Giải mã OTP
    int encryptedLength = encryptedOTPLength - AES_BLOCK_SIZE;  // Vì IV đã được tách ra
    unsigned char decrypted[encryptedLength];
    AES_cbc_encrypt(encryptedData + AES_BLOCK_SIZE, decrypted, encryptedLength, &decryptKey, iv, AES_DECRYPT);

    // Loại bỏ padding (giả sử padding là 0, bạn có thể thay đổi theo nhu cầu)
    int i;
    for (i = encryptedLength - 1; i >= 0; i--) {
        if (decrypted[i] != 0) {
            break;
        }
    }

    // Tạo chuỗi kết quả (decrypted) thành một chuỗi UTF-8
    std::string decryptedStr(reinterpret_cast<char*>(decrypted), i + 1);

    return env->NewStringUTF(decryptedStr.c_str());
}