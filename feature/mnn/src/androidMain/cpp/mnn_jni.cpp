#include <jni.h>
#include <MNN/Interpreter.hpp>
#include <MNN/expr/ExecutorScope.hpp>
#include <diffusion/stable_diffusion.hpp>
#include <atomic>
#include <cstdlib>
#include <stdexcept>
#include <string>
#include <sys/stat.h>

namespace {
std::atomic<bool> isCancelled{false};

std::string fromJava(JNIEnv* env, jstring value) {
    if (!value) throw std::invalid_argument("Missing MNN argument");
    const char* chars = env->GetStringUTFChars(value, nullptr);
    if (!chars) throw std::runtime_error("MNN string allocation failed");
    std::string result(chars);
    env->ReleaseStringUTFChars(value, chars);
    return result;
}
}

extern "C" JNIEXPORT void JNICALL
Java_com_shifthackz_aisdv1_feature_mnn_MnnNativeBridgeImpl_interruptNative(JNIEnv*, jobject) {
    isCancelled.store(true);
}

extern "C" JNIEXPORT void JNICALL
Java_com_shifthackz_aisdv1_feature_mnn_MnnNativeBridgeImpl_generateNative(
    JNIEnv* env, jobject owner, jstring model, jbyteArray prompt, jstring output,
    jstring cache, jint steps, jint seed, jboolean isOpenCl) {
    try {
        isCancelled.store(false);
        if (steps < 1 || steps > 50 || seed < 0) throw std::invalid_argument("Unsupported MNN parameters");
        auto modelPath = fromJava(env, model);
        auto outputPath = fromJava(env, output);
        if (!prompt) throw std::invalid_argument("Missing MNN prompt");
        std::string text(env->GetArrayLength(prompt), '\0');
        env->GetByteArrayRegion(prompt, 0, text.size(), reinterpret_cast<jbyte*>(text.data()));
        if (env->ExceptionCheck()) throw std::runtime_error("MNN prompt allocation failed");
        auto cachePath = fromJava(env, cache);
        if (setenv("SDAI_MNN_CACHE", cachePath.c_str(), 1) != 0) throw std::runtime_error("MNN cache setup failed");
        auto backend = isOpenCl ? MNN_FORWARD_OPENCL : MNN_FORWARD_CPU;
        MNN::ScheduleConfig config;
        config.type = backend;
        auto available = MNN::Interpreter::createRuntime({config});
        if (available.first.find(backend) == available.first.end()) {
            throw std::runtime_error("Requested MNN backend unavailable");
        }
        MNN::BackendConfig backendConfig;
        MNN::Express::ExecutorScope scope(MNN::Express::Executor::newExecutor(backend, backendConfig, 4));
        auto runtime = MNN::DIFFUSION::Diffusion::createDiffusion(
            modelPath, MNN::DIFFUSION::STABLE_DIFFUSION_1_5, backend, 0);
        if (!runtime) throw std::runtime_error("MNN diffusion creation failed");
        std::unique_ptr<MNN::DIFFUSION::Diffusion> owned(runtime);
        if (isCancelled.load() || !runtime->load()) throw std::runtime_error("MNN model loading failed or cancelled");
        jclass clazz = env->GetObjectClass(owner);
        jmethodID progress = env->GetMethodID(clazz, "onNativeProgress", "(I)V");
        if (!progress) throw std::runtime_error("MNN progress callback unavailable");
        auto callback = [&](int percent) {
            if (isCancelled.load()) throw std::runtime_error("MNN generation cancelled");
            env->CallVoidMethod(owner, progress, percent);
            if (env->ExceptionCheck()) throw std::runtime_error("MNN callback failed");
        };
        if (!runtime->run(text, outputPath, steps, seed, callback)) throw std::runtime_error("MNN image writing failed");
        if (isCancelled.load()) throw std::runtime_error("MNN generation cancelled");
        struct stat result {};
        if (stat(outputPath.c_str(), &result) != 0 || result.st_size <= 0 || result.st_size > 16 * 1024 * 1024) {
            throw std::runtime_error("Invalid MNN output file");
        }
    } catch (const std::exception& failure) {
        if (!env->ExceptionCheck()) env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), failure.what());
    } catch (...) {
        if (!env->ExceptionCheck()) env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), "MNN native failure");
    }
}
