#pragma once
#include <MNN/Interpreter.hpp>
#include <MNN/Tensor.hpp>
#include <algorithm>
#include <cmath>
#include <cstdint>
#include <functional>
#include <memory>
#include <stdexcept>
#include <vector>

namespace sdai {
// Runs bounded RGB tiles with a halo; only each tile's central output is copied.
// Caller owns input/output buffers and cancellation until the session has drained.
inline void upscale(const char* model, const uint8_t* rgb, int width, int height,
                    uint8_t* output, const std::function<bool()>& isCancelled,
                    const std::function<void(int)>& progress) {
    if (!model || !rgb || !output || width < 1 || height < 1 || width > 1024 || height > 1024)
        throw std::invalid_argument("Invalid upscaler input");
    auto checkCancellation = [&] {
        if (isCancelled()) throw std::runtime_error("Upscaling cancelled");
    };
    checkCancellation();
    std::unique_ptr<MNN::Interpreter> interpreter(MNN::Interpreter::createFromFile(model));
    if (!interpreter) throw std::runtime_error("MNN upscaler model could not be loaded");
    MNN::ScheduleConfig config;
    config.type = MNN_FORWARD_CPU;
    config.numThread = 4;
    // Allocate only after replacing the model's exported shape with a bounded tile.
    interpreter->setSessionMode(MNN::Interpreter::Session_Resize_Defer);
    auto session = interpreter->createSession(config);
    if (!session) throw std::runtime_error("MNN upscaler session could not be created");
    auto input = interpreter->getSessionInput(session, nullptr);
    if (!input || input->dimensions() != 4) throw std::runtime_error("Invalid MNN upscaler input tensor");
    constexpr int tile = 64;
    constexpr int halo = 16;
    constexpr int scale = 4;
    const int total = ((width + tile - 1) / tile) * ((height + tile - 1) / tile);
    int completed = 0;
    for (int y = 0; y < height; y += tile) {
        for (int x = 0; x < width; x += tile) {
            checkCancellation();
            const int left = std::max(0, x - halo), top = std::max(0, y - halo);
            const int right = std::min(width, x + tile + halo), bottom = std::min(height, y + tile + halo);
            const int tw = right - left, th = bottom - top;
            const bool isTensorFlow = input->getDimensionType() == MNN::Tensor::TENSORFLOW;
            interpreter->resizeTensor(input, isTensorFlow
                ? std::vector<int>{1, th, tw, 3} : std::vector<int>{1, 3, th, tw});
            interpreter->resizeSession(session);
            MNN::Tensor hostInput(input, MNN::Tensor::CAFFE);
            float* values = hostInput.host<float>();
            if (!values) throw std::runtime_error("MNN input allocation failed");
            for (int c = 0; c < 3; ++c)
                for (int iy = 0; iy < th; ++iy)
                    for (int ix = 0; ix < tw; ++ix)
                        values[(c * th + iy) * tw + ix] =
                            rgb[((top + iy) * width + left + ix) * 3 + c] / 255.0f;
            if (!input->copyFromHostTensor(&hostInput) || interpreter->runSession(session) != MNN::NO_ERROR)
                throw std::runtime_error("MNN upscaler inference failed");
            checkCancellation();
            auto result = interpreter->getSessionOutput(session, nullptr);
            if (!result) throw std::runtime_error("MNN upscaler returned no output");
            MNN::Tensor hostOutput(result, MNN::Tensor::CAFFE);
            if (!result->copyToHostTensor(&hostOutput) || hostOutput.channel() != 3 ||
                hostOutput.width() != tw * scale || hostOutput.height() != th * scale)
                throw std::runtime_error("Unexpected MNN upscaler output shape");
            const float* pixels = hostOutput.host<float>();
            if (!pixels) throw std::runtime_error("MNN output allocation failed");
            const int ow = tw * scale, oh = th * scale;
            const int copyWidth = std::min(tile, width - x) * scale;
            const int copyHeight = std::min(tile, height - y) * scale;
            for (int iy = 0; iy < copyHeight; ++iy)
                for (int ix = 0; ix < copyWidth; ++ix)
                    for (int c = 0; c < 3; ++c) {
                        const float value = pixels[(c * oh + (y - top) * scale + iy) * ow + (x - left) * scale + ix];
                        if (!std::isfinite(value)) throw std::runtime_error("Nonfinite MNN output");
                        output[((y * scale + iy) * width * scale + x * scale + ix) * 3 + c] =
                            static_cast<uint8_t>(std::round(std::clamp(value, 0.0f, 1.0f) * 255.0f));
                    }
            progress(++completed * 100 / total);
        }
    }
    checkCancellation();
}
}
