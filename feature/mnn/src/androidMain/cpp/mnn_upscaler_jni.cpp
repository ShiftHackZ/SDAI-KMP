#include <jni.h>
#include <atomic>
#include <vector>
#include <string>
#include "sdai_upscaler.hpp"

struct UpscalerAttempt { std::atomic<bool> isCancelled{false}; };

extern "C" JNIEXPORT jlong JNICALL
Java_com_shifthackz_aisdv1_feature_mnn_MnnUpscalerNativeBridgeImpl_createNative(JNIEnv* env, jobject) {
    try { return reinterpret_cast<jlong>(new UpscalerAttempt()); }
    catch (...) { env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), "MNN upscaler allocation failed"); return 0; }
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_shifthackz_aisdv1_feature_mnn_MnnUpscalerNativeBridgeImpl_runNative(
    JNIEnv* env, jobject self, jlong handle, jstring model, jbyteArray input, jint width, jint height) {
    auto attempt = reinterpret_cast<UpscalerAttempt*>(handle);
    if (!attempt) return nullptr;
    try {
        if (width < 1 || height < 1 || width > 1024 || height > 1024 ||
            env->GetArrayLength(input) != width * height * 3)
            throw std::invalid_argument("Invalid MNN upscaler pixels");
        const char* modelChars = env->GetStringUTFChars(model, nullptr);
        if (!modelChars) return nullptr;
        std::string path(modelChars);
        env->ReleaseStringUTFChars(model, modelChars);
        std::vector<uint8_t> pixels(width * height * 3);
        env->GetByteArrayRegion(input, 0, pixels.size(), reinterpret_cast<jbyte*>(pixels.data()));
        std::vector<uint8_t> output(width * height * 16 * 3);
        auto callback = env->GetMethodID(env->GetObjectClass(self), "onProgress", "(I)V");
        sdai::upscale(path.c_str(), pixels.data(), width, height, output.data(),
            [&] { return attempt->isCancelled.load() || env->ExceptionCheck(); },
            [&](int progress) { env->CallVoidMethod(self, callback, progress); });
        auto result = env->NewByteArray(output.size());
        if (result) env->SetByteArrayRegion(result, 0, output.size(), reinterpret_cast<const jbyte*>(output.data()));
        return result;
    } catch (const std::exception& failure) {
        if (!env->ExceptionCheck()) env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), failure.what());
        return nullptr;
    }
}
extern "C" JNIEXPORT void JNICALL
Java_com_shifthackz_aisdv1_feature_mnn_MnnUpscalerNativeBridgeImpl_interruptNative(JNIEnv*, jobject, jlong handle) {
    if (auto attempt = reinterpret_cast<UpscalerAttempt*>(handle)) attempt->isCancelled.store(true);
}
extern "C" JNIEXPORT void JNICALL
Java_com_shifthackz_aisdv1_feature_mnn_MnnUpscalerNativeBridgeImpl_destroyNative(JNIEnv*, jobject, jlong handle) {
    delete reinterpret_cast<UpscalerAttempt*>(handle);
}
