#include <openssl/aes.h>
#include <openssl/rand.h>
#include <openssl/evp.h>
#include <openssl/sha.h>
#include <sstream>
#include <iostream>
#include <cstring>
#include <iomanip>
#include <jni.h>

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_example_presentation_main_setting_security_manager_OTPUtils_encryptOTP(JNIEnv *env,
                                                                                jobject thiz,
                                                                                jstring otp,
                                                                                jstring secret_key) {
    const char *otpStr = env->GetStringUTFChars(otp, nullptr);
    const char *secret = env->GetStringUTFChars(secret_key, nullptr);

    std::string otpInput(otpStr);
    std::string key(secret);

    // Tạo khóa AES từ secretKey
    unsigned char aesKey[32];
    SHA256_CTX sha256Ctx;
    SHA256_Init(&sha256Ctx);
    SHA256_Update(&sha256Ctx, key.c_str(), key.size());
    SHA256_Final(aesKey, &sha256Ctx);

    // Khởi tạo IV
    unsigned char iv[AES_BLOCK_SIZE];
    // Khởi tạo IV (Initialization Vector) ngẫu nhiên
    if (!RAND_bytes(iv, AES_BLOCK_SIZE)) {
        std::cerr << "Error generating IV." << std::endl;
        env->ReleaseStringUTFChars(otp, otpStr);
        env->ReleaseStringUTFChars(secret_key, secret);
        return nullptr;
    }

    // Mã hóa OTP sử dụng AES (chế độ CBC)
    EVP_CIPHER_CTX *ctx = EVP_CIPHER_CTX_new();
    int len;
    int ciphertext_len;
    unsigned char ciphertext[128];

    if (!EVP_EncryptInit_ex(ctx, EVP_aes_256_cbc(), nullptr, aesKey, iv)) {
        std::cerr << "Error initializing encryption." << std::endl;
        EVP_CIPHER_CTX_free(ctx);
        env->ReleaseStringUTFChars(otp, otpStr);
        env->ReleaseStringUTFChars(secret_key, secret);
        return nullptr;
    }

    if (!EVP_EncryptUpdate(ctx, ciphertext, &len, (unsigned char *) otpInput.c_str(),
                           otpInput.size())) {
        std::cerr << "Error during encryption." << std::endl;
        EVP_CIPHER_CTX_free(ctx);
        env->ReleaseStringUTFChars(otp, otpStr);
        env->ReleaseStringUTFChars(secret_key, secret);
        return nullptr;
    }
    ciphertext_len = len;

    if (!EVP_EncryptFinal_ex(ctx, ciphertext + len, &len)) {
        std::cerr << "Error finalizing encryption." << std::endl;
        EVP_CIPHER_CTX_free(ctx);
        env->ReleaseStringUTFChars(otp, otpStr);
        env->ReleaseStringUTFChars(secret_key, secret);
        return nullptr;
    }
    ciphertext_len += len;

    EVP_CIPHER_CTX_free(ctx);

    // Kết hợp IV và ciphertext thành một chuỗi hex
    std::ostringstream hexStream;
    for (int i = 0; i < AES_BLOCK_SIZE; i++) {
        hexStream << std::hex << std::setw(2) << std::setfill('0') << (int) iv[i];
    }
    for (int i = 0; i < ciphertext_len; i++) {
        hexStream << std::hex << std::setw(2) << std::setfill('0') << (int) ciphertext[i];
    }

    std::string encryptedHex = hexStream.str();

    env->ReleaseStringUTFChars(otp, otpStr);
    env->ReleaseStringUTFChars(secret_key, secret);

    return env->NewStringUTF(encryptedHex.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_example_presentation_main_setting_security_manager_OTPUtils_decryptOTP(JNIEnv *env,
                                                                                jobject /* this */,
                                                                                jstring encryptedOTP,
                                                                                jstring secretKey) {
    const char *encryptedOTPStr = env->GetStringUTFChars(encryptedOTP, nullptr);
    const char *secret = env->GetStringUTFChars(secretKey, nullptr);

    // Chuyển chuỗi hex thành mảng byte
    int encryptedOTPLength = strlen(encryptedOTPStr) / 2;
    unsigned char encryptedData[encryptedOTPLength];
    for (int i = 0; i < encryptedOTPLength; i++) {
        sscanf(&encryptedOTPStr[i * 2], "%2hhx", &encryptedData[i]);
    }

    // Tạo khóa AES từ secretKey
    unsigned char aesKey[32];
    SHA256_CTX sha256Ctx;
    SHA256_Init(&sha256Ctx);
    SHA256_Update(&sha256Ctx, secret, strlen(secret));
    SHA256_Final(aesKey, &sha256Ctx);

    // Tách IV từ dữ liệu mã hóa
    unsigned char iv[AES_BLOCK_SIZE];
    std::memcpy(iv, encryptedData, AES_BLOCK_SIZE);

    // Giải mã OTP
    EVP_CIPHER_CTX *ctx = EVP_CIPHER_CTX_new();
    int len;
    int plaintext_len;
    unsigned char plaintext[128];

    if (!EVP_DecryptInit_ex(ctx, EVP_aes_256_cbc(), nullptr, aesKey, iv)) {
        std::cerr << "Error initializing decryption." << std::endl;
        EVP_CIPHER_CTX_free(ctx);
        env->ReleaseStringUTFChars(encryptedOTP, encryptedOTPStr);
        env->ReleaseStringUTFChars(secretKey, secret);
        return nullptr;
    }

    if (!EVP_DecryptUpdate(ctx, plaintext, &len, encryptedData + AES_BLOCK_SIZE,
                           encryptedOTPLength - AES_BLOCK_SIZE)) {
        std::cerr << "Error during decryption." << std::endl;
        EVP_CIPHER_CTX_free(ctx);
        env->ReleaseStringUTFChars(encryptedOTP, encryptedOTPStr);
        env->ReleaseStringUTFChars(secretKey, secret);
        return nullptr;
    }
    plaintext_len = len;

    if (!EVP_DecryptFinal_ex(ctx, plaintext + len, &len)) {
        std::cerr << "Error finalizing decryption." << std::endl;
        EVP_CIPHER_CTX_free(ctx);
        env->ReleaseStringUTFChars(encryptedOTP, encryptedOTPStr);
        env->ReleaseStringUTFChars(secretKey, secret);
        return nullptr;
    }
    plaintext_len += len;

    EVP_CIPHER_CTX_free(ctx);

    // Tạo chuỗi kết quả
    std::string decryptedStr(reinterpret_cast<char *>(plaintext), plaintext_len);

    env->ReleaseStringUTFChars(encryptedOTP, encryptedOTPStr);
    env->ReleaseStringUTFChars(secretKey, secret);

    return env->NewStringUTF(decryptedStr.c_str());
}