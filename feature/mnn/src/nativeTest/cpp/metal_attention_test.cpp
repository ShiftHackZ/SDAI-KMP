#include "scenario.hpp"
#include "MNN_generated.h"
#include <MNN/expr/ExprCreator.hpp>
#include <MNN/expr/ExecutorScope.hpp>
#include "core/Backend.hpp"
#include "core/TensorUtils.hpp"
#include <algorithm>
#include <cmath>

using namespace MNN;
using namespace MNN::Express;

static std::vector<float> values(int size, int offset) {
    std::vector<float> result(size);
    for (int index = 0; index < size; ++index) result[index] = std::sin(float(index + offset) * .173f) * .3f;
    return result;
}

int main() {
    Scenarios tests;
    for (int depth : {40, 80, 160}) {
        for (int queryLength : {5, 65}) {
        for (int keyLength : {queryLength, queryLength + 2}) {
            tests.run("given noncausal Metal attention, head depth " + std::to_string(depth) +
                " with query length " + std::to_string(queryLength) + " and key length " + std::to_string(keyLength) +
                ", expected dense CPU numeric parity on the Metal backend", [&] {
                // Given
                const int batch = 2;
                auto q = values(batch * queryLength * depth, 1);
                auto k = values(batch * keyLength * depth, 19);
                auto v = values(batch * keyLength * depth, 41);
                std::vector<float> expected;
                BackendConfig config;
                config.precision = BackendConfig::Precision_High;
                {
                    ExecutorScope scope(Executor::newExecutor(MNN_FORWARD_CPU, config, 1));
                    auto query = _Const(q.data(), {batch, queryLength, depth}, NCHW);
                    auto key = _Const(k.data(), {batch, keyLength, depth}, NCHW);
                    auto value = _Const(v.data(), {batch, keyLength, depth}, NCHW);
                    auto dense = _MatMul(_Softmax(_MatMul(query, key, false, true) * _Scalar(1.f / std::sqrt(float(depth))), -1), value);
                    const float* data = dense->readMap<float>();
                    expect(data != nullptr, "Dense CPU reference failed");
                    expected.assign(data, data + batch * queryLength * depth);
                }
                ExecutorScope scope(Executor::newExecutor(MNN_FORWARD_METAL, config, 1));
                auto query = _Input({batch, queryLength, 1, depth}, NCHW);
                auto key = _Input({batch, keyLength, 1, depth}, NCHW);
                auto value = _Input({batch, keyLength, 1, depth}, NCHW);
                std::copy(q.begin(), q.end(), query->writeMap<float>());
                std::copy(k.begin(), k.end(), key->writeMap<float>());
                std::copy(v.begin(), v.end(), value->writeMap<float>());
                OpT op;
                op.type = OpType_Attention;
                AttentionParamT param;
                param.kv_cache = false;
                param.output_c4 = false;
                op.main.Set(std::move(param));
                // When
                auto fused = Variable::create(Expr::create(&op, {query, key, value}));
                const float* actual = fused->readMap<float>();
                // Then
                expect(actual != nullptr && fused->getInfo()->dim == std::vector<int>({batch, queryLength, depth}), "Fused output layout differs");
                const auto* backend = TensorUtils::getDescribeOrigin(fused->getTensor())->getBackend();
                expect(backend && backend->type() == MNN_FORWARD_METAL, "Attention fell back from Metal");
                float maxError = 0.f;
                for (int index = 0; index < expected.size(); ++index) {
                    expect(std::isfinite(actual[index]), "Attention returned nonfinite output");
                    maxError = std::max(maxError, std::abs(actual[index] - expected[index]));
                }
                expect(maxError < .002f, "Metal attention differs from noncausal dense reference");
                std::cout << "Metal F32 maximum absolute residual=" << maxError << '\n';
            });
        }
        }
    }
    return tests.finish();
}
