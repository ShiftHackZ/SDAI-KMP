#include "MnnBridge.h"
#include "Runtime/sdai_attempt.hpp"
#include <MNN/Interpreter.hpp>
#include <MNN/expr/ExecutorScope.hpp>
#include <diffusion/stable_diffusion.hpp>
#include <atomic>
#include <memory>
#include <stdexcept>
#include <string>
#include <sys/stat.h>
#import <Metal/Metal.h>

struct MnnAttempt {
    std::atomic<bool> isCancelled{false};
    std::atomic<bool> hasStarted{false};
};

extern "C" MnnAttempt *mnn_attempt_create(void) {
    try { return new MnnAttempt(); } catch (...) { return nullptr; }
}
extern "C" int mnn_metal_available(void) {
    return MTLCreateSystemDefaultDevice() != nil;
}
extern "C" void mnn_attempt_interrupt(MnnAttempt *attempt) {
    if (attempt) attempt->isCancelled.store(true);
}
extern "C" void mnn_attempt_destroy(MnnAttempt *attempt) { delete attempt; }

extern "C" int mnn_attempt_generate(MnnAttempt *attempt, const char *model,
    const char *prompt, const char *output, const char *workspace, int steps,
    int32_t seed, int use_metal, MnnProgress progress, void *context) {
    if (!attempt) return 2;
    if (attempt->hasStarted.exchange(true)) return 2;
    try {
        if (!model || !prompt || !output || !workspace || steps < 1 || steps > 50 || seed < 0)
            throw std::invalid_argument("Unsupported MNN request");
        auto checkCancellation = [&] {
            if (attempt->isCancelled.load()) throw std::runtime_error("MNN generation cancelled");
        };
        checkCancellation();
        if (use_metal && !mnn_metal_available()) throw std::runtime_error("MNN Metal unavailable");
        sdai::AttemptConfiguration configuration(std::string(workspace) + "/backend.cache");
        auto backend = use_metal ? MNN_FORWARD_METAL : MNN_FORWARD_CPU;
        const int executorMode = use_metal ? MNN_GPU_TUNING_NONE : 4;
        MNN::ScheduleConfig config;
        config.type = backend;
        config.numThread = executorMode;
        auto available = MNN::Interpreter::createRuntime({config});
        if (available.first.find(backend) == available.first.end())
            throw std::runtime_error("Requested MNN backend unavailable");
        MNN::BackendConfig backendConfig;
        MNN::Express::ExecutorScope scope(MNN::Express::Executor::newExecutor(backend, backendConfig, executorMode));
        std::unique_ptr<MNN::DIFFUSION::Diffusion> runtime(
            MNN::DIFFUSION::Diffusion::createDiffusion(model,
                MNN::DIFFUSION::STABLE_DIFFUSION_1_5, backend, 0));
        if (!runtime || !runtime->load()) throw std::runtime_error("MNN model loading failed");
        checkCancellation();
        auto callback = [&](int percent) {
            checkCancellation();
            if (progress) progress(context, percent);
            checkCancellation();
        };
        if (!runtime->run(prompt, output, steps, seed, callback))
            throw std::runtime_error("MNN output writing failed");
        checkCancellation();
        struct stat info{};
        if (stat(output, &info) || info.st_size < 24 || info.st_size > 16 * 1024 * 1024)
            throw std::runtime_error("Invalid MNN output");
        return 0;
    } catch (...) {
        // Swift receives a bounded typed failure; no C++ exception crosses the ABI.
    }
    return attempt->isCancelled.load() ? 1 : 2;
}
