#include "scenario.hpp"
#include "MNN_generated.h"
#include <MNN/Interpreter.hpp>
#include <MNN/expr/ExprCreator.hpp>
#include <MNN/expr/ExecutorScope.hpp>
#include "core/Backend.hpp"
#include "core/Execution.hpp"
#include "core/TensorUtils.hpp"
#import <Metal/Metal.h>
#include <algorithm>
#include <cmath>
#include <memory>

using namespace MNN;
using namespace MNN::Express;

int main() {
    Scenarios tests;
    tests.run("given low-memory Metal weight construction, sixteen packed convolution executions remain alive, expected only packed weights and bounded upload memory", [&] {
        // Given
        @autoreleasepool {
            BackendConfig options;
            options.precision = BackendConfig::Precision_Low;
            options.memory = BackendConfig::Memory_Low;
            ScheduleConfig config;
            config.type = MNN_FORWARD_METAL;
            config.backendConfig = &options;
            auto runtimes = Interpreter::createRuntime({config});
            auto runtime = runtimes.first.find(MNN_FORWARD_METAL);
            expect(runtime != runtimes.first.end(), "Metal runtime unavailable");
            auto hints = runtime->second->hint();
            hints.winogradMemoryUsed = 0;
            runtime->second->setRuntimeHint(hints);
            std::unique_ptr<Backend> backend(runtime->second->onCreate(&options));
            expect(backend && backend->type() == MNN_FORWARD_METAL, "Backend changed");
            id<MTLDevice> device = MTLCreateSystemDefaultDevice();
            OpT op;
            op.type = OpType_Convolution;
            Convolution2DT conv;
            conv.common = std::make_unique<Convolution2DCommonT>();
            conv.common->inputCount = 512; conv.common->outputCount = 512;
            conv.common->kernelX = 3; conv.common->kernelY = 3;
            conv.common->strideX = 1; conv.common->strideY = 1;
            conv.common->dilateX = 1; conv.common->dilateY = 1;
            conv.common->group = 1; conv.common->padMode = PadMode_SAME;
            conv.weight.assign(512 * 512 * 9, .001f); conv.bias.assign(512, 0.f);
            op.main.Set(std::move(conv));
            flatbuffers::FlatBufferBuilder builder;
            builder.Finish(Op::Pack(builder, &op));
            const auto* packed = flatbuffers::GetRoot<Op>(builder.GetBufferPointer());
            std::unique_ptr<Tensor> input(Tensor::createDevice<float>({1, 512, 8, 8}, Tensor::CAFFE));
            std::unique_ptr<Tensor> output(Tensor::createDevice<float>({1, 512, 8, 8}, Tensor::CAFFE));
            std::vector<std::unique_ptr<Execution>> executions;
            const auto initial = device.currentAllocatedSize;
            // When
            for (int index = 0; index < 16; ++index) {
                executions.emplace_back(backend->onCreate({input.get()}, {output.get()}, packed));
                // Then
                expect(executions.back() && executions.back()->valid(), "Packed execution lost");
                const size_t retainedWeights = (512u * 512 * 9 * 2 + 512 * 2) * (index + 1);
                const size_t used = device.currentAllocatedSize - initial;
                expect(used <= retainedWeights + (16u << 20), "Completed F32 upload buffers accumulated");
            }
            std::cout << "Metal retained allocation bytes=" << device.currentAllocatedSize - initial << '\n';
        }
    });
    tests.run("given packed Metal convolution weights, construction autorelease pool has drained, expected finite CPU-reference output on Metal", [&] {
        // Given
        std::vector<float> weights(8 * 8 * 9), inputValues(8 * 8 * 8);
        for (int index = 0; index < weights.size(); ++index) weights[index] = std::sin(float(index) * .1f) * .02f;
        for (int index = 0; index < inputValues.size(); ++index) inputValues[index] = std::cos(float(index) * .2f);
        std::vector<float> expected;
        auto convolution = [&] {
            auto input = _Input({1, 8, 8, 8}, NCHW);
            std::copy(inputValues.begin(), inputValues.end(), input->writeMap<float>());
            return _Conv(std::vector<float>(weights), std::vector<float>(8, .01f), input, {8, 8}, {3, 3}, SAME);
        };
        BackendConfig config;
        config.precision = BackendConfig::Precision_High;
        {
            ExecutorScope scope(Executor::newExecutor(MNN_FORWARD_CPU, config, 1));
            auto output = _Convert(convolution(), NCHW);
            const float* data = output->readMap<float>();
            expect(data && output->getInfo()->size == 512, "CPU reference failed");
            expected.assign(data, data + 512);
        }
        config.precision = BackendConfig::Precision_Low;
        config.memory = BackendConfig::Memory_Low;
        ExecutorScope scope(Executor::newExecutor(MNN_FORWARD_METAL, config, 1));
        for (auto& entry : Executor::getRuntime().first) {
            auto hints = entry.second->hint();
            hints.winogradMemoryUsed = 0;
            entry.second->setRuntimeHint(hints);
        }
        VARP convolutionOutput;
        @autoreleasepool {
            convolutionOutput = convolution();
            Variable::prepareCompute({convolutionOutput});
        }
        // When
        auto output = _Convert(convolutionOutput, NCHW);
        const float* actual = output->readMap<float>();
        // Then
        expect(actual && output->getInfo()->size == 512, "Packed weight output unavailable");
        const auto* backend = TensorUtils::getDescribeOrigin(convolutionOutput->getTensor())->getBackend();
        expect(backend && backend->type() == MNN_FORWARD_METAL, "Convolution fell back from Metal");
        float maxError = 0.f;
        for (int index = 0; index < expected.size(); ++index) {
            expect(std::isfinite(actual[index]), "Packed convolution returned nonfinite output");
            maxError = std::max(maxError, std::abs(actual[index] - expected[index]));
        }
        expect(maxError < .003f, "Packed weights changed convolution values");
        std::cout << "Metal FP16 convolution maximum absolute residual=" << maxError << '\n';
    });
    return tests.finish();
}
