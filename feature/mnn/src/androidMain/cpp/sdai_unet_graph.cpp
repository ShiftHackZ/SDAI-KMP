#include "sdai_unet_graph.hpp"
#include "MNN_generated.h"
#include <algorithm>
#include <cstring>
#include <map>
#include <limits>
#include <set>
#include <stdexcept>
#include <utility>

namespace sdai {
namespace {
using MNN::NetT;
using MNN::OpT;

void require(bool condition) {
    if (!condition) throw std::runtime_error("Pinned MNN UNet zero-bias structure mismatch");
}

// Reserved tokens denote dimensions, not numeric values. Shape arithmetic below
// is permitted only on small literal integers; dimensions can only be
// selected, reshaped and concatenated into the expected bias shape.
constexpr int64_t kDimension = std::numeric_limits<int64_t>::min();
struct ShapeValue {
    std::vector<int64_t> values;
    bool isScalar;
};

class Graph {
public:
    explicit Graph(NetT& net) {
        require(net.subgraphs.empty() && !net.tensorName.empty());
        for (const auto& op : net.oplists) {
            require(op != nullptr);
            for (int tensor : op->outputIndexes) {
                require(tensor >= 0 && tensor < net.tensorName.size());
                require(producers.emplace(tensor, op.get()).second);
            }
            for (int tensor : op->inputIndexes) {
                require(tensor >= 0 && tensor < net.tensorName.size());
                ++consumers[tensor];
            }
        }
    }

    OpT& producer(int tensor) const {
        auto found = producers.find(tensor);
        require(found != producers.end());
        return *found->second;
    }

    bool isPositiveZero(int tensor) const {
        const auto& op = producer(tensor);
        const auto* blob = op.main.AsBlob();
        if (op.type != MNN::OpType_Const || !blob || blob->dataType != MNN::DataType_DT_FLOAT ||
            !blob->external.empty() || blob->float32s.size() != 1 ||
            !(blob->dims.empty() || blob->dims == std::vector<int>{1})) return false;
        uint32_t bits;
        std::memcpy(&bits, blob->float32s.data(), sizeof(bits));
        return bits == 0;
    }

    void requireBinary(const OpT& op, MNN::BinaryOpOperation operation) const {
        const auto* binary = op.main.AsBinaryOp();
        require(op.type == MNN::OpType_BinaryOp && op.inputIndexes.size() == 2 &&
            op.outputIndexes.size() == 1 && binary && binary->T == MNN::DataType_DT_FLOAT &&
            binary->opType == operation && binary->activationType == 0);
    }

    void requireRankThreeReshape(int tensor) const {
        const auto& reshape = producer(tensor);
        require(reshape.type == MNN::OpType_Reshape && reshape.inputIndexes.size() == 2);
        const auto& shape = producer(reshape.inputIndexes[1]);
        require(shape.type == MNN::OpType_Concat && shape.inputIndexes.size() == 3);
        for (int part : shape.inputIndexes) {
            const auto& unsqueeze = producer(part);
            require(unsqueeze.type == MNN::OpType_Unsqueeze);
        }
    }

    ShapeValue shapeValue(int tensor, int query, int key, int depth = 0) const {
        require(depth < 128);
        const auto& op = producer(tensor);
        auto input = [&](size_t index) {
            require(index < op.inputIndexes.size());
            return shapeValue(op.inputIndexes[index], query, key, depth + 1);
        };
        auto scalar = [&](size_t index) {
            auto value = input(index);
            require(value.values.size() == 1 && value.values[0] >= 0);
            return value.values[0];
        };
        switch (op.type) {
            case MNN::OpType_Const: {
                const auto* blob = op.main.AsBlob();
                require(blob && blob->external.empty() &&
                    (blob->dataType == MNN::DataType_DT_INT32 || blob->dataType == MNN::DataType_DT_INT64));
                require(blob->dims.empty() || (blob->dims.size() == 1 && blob->dims[0] <= 4));
                std::vector<int64_t> values;
                if (blob->dataType == MNN::DataType_DT_INT32) values.assign(blob->int32s.begin(), blob->int32s.end());
                else values = blob->int64s;
                require(values.size() <= 4 && (blob->dims.empty() ? values.size() == 1 : values.size() == blob->dims[0]));
                return {values, blob->dims.empty()};
            }
            case MNN::OpType_Shape: {
                require(op.inputIndexes.size() == 1);
                if (op.inputIndexes[0] == query) return {{kDimension + 1, kDimension + 2, kDimension + 3}, false};
                if (op.inputIndexes[0] == key) return {{kDimension + 4, kDimension + 5, kDimension + 6}, false};
                auto value = input(0);
                return {value.isScalar ? std::vector<int64_t>{} : std::vector<int64_t>{static_cast<int64_t>(value.values.size())}, false};
            }
            case MNN::OpType_Rank: {
                auto value = input(0);
                return {{value.isScalar ? 0 : 1}, true};
            }
            case MNN::OpType_Unsqueeze: {
                auto value = input(0);
                require(value.isScalar && value.values.size() == 1);
                if (op.inputIndexes.size() == 2) require(scalar(1) == 0);
                else {
                    const auto* param = op.main.AsSqueezeParam();
                    require(param && param->squeezeDims == std::vector<int>{0});
                }
                value.isScalar = false;
                return value;
            }
            case MNN::OpType_Squeeze: {
                auto value = input(0);
                require(!value.isScalar && value.values.size() == 1);
                const auto* param = op.main.AsSqueezeParam();
                require(param && (param->squeezeDims.empty() || param->squeezeDims == std::vector<int>{0}));
                value.isScalar = true;
                return value;
            }
            case MNN::OpType_BinaryOp: {
                const auto* param = op.main.AsBinaryOp();
                // This export leaves BinaryOp.T at FLOAT even in integer shape
                // helpers; MNN selects the integer kernel from input tensors.
                require(param && param->T == MNN::DataType_DT_FLOAT && param->activationType == 0);
                auto left = input(0);
                auto right = input(1);
                require(left.values.size() == 1 && right.values.size() == 1 &&
                    left.values[0] >= -4 && left.values[0] <= 4 && right.values[0] >= -4 && right.values[0] <= 4);
                int64_t value;
                if (param->opType == MNN::BinaryOpOperation_ADD) value = left.values[0] + right.values[0];
                else if (param->opType == MNN::BinaryOpOperation_MOD) {
                    require(right.values[0] > 0);
                    value = left.values[0] % right.values[0];
                } else {
                    require(param->opType == MNN::BinaryOpOperation_SUB);
                    value = left.values[0] - right.values[0];
                }
                require(value >= -4 && value <= 4);
                return {{value}, left.isScalar && right.isScalar};
            }
            case MNN::OpType_StridedSlice: {
                require(op.inputIndexes.size() == 4);
                const auto* param = op.main.AsStridedSliceParam();
                require(param && (param->beginMask == 0 || param->beginMask == 1) &&
                    (param->endMask == 0 || param->endMask == 1) && param->ellipsisMask == 0 &&
                    param->newAxisMask == 0 && param->shrinkAxisMask == 0);
                auto value = input(0);
                const auto begin = param->beginMask ? 0 : scalar(1);
                const auto end = param->endMask ? static_cast<int64_t>(value.values.size()) : scalar(2);
                const auto stride = scalar(3);
                require(!value.isScalar && stride == 1 && begin <= end && end <= value.values.size());
                return {std::vector<int64_t>(value.values.begin() + begin, value.values.begin() + end), false};
            }
            case MNN::OpType_GatherV2: {
                require(op.inputIndexes.size() == 3 && scalar(2) == 0);
                auto value = input(0);
                auto index = input(1);
                require(!value.isScalar && index.values.size() == 1 && index.values[0] >= 0 && index.values[0] < value.values.size());
                return {{value.values[index.values[0]]}, index.isScalar};
            }
            case MNN::OpType_Concat: {
                const auto* param = op.main.AsAxis();
                require(param && param->axis == 0);
                std::vector<int64_t> result;
                for (size_t i = 0; i < op.inputIndexes.size(); ++i) {
                    auto part = input(i);
                    require(!part.isScalar);
                    result.insert(result.end(), part.values.begin(), part.values.end());
                }
                require(result.size() <= 4);
                return {result, false};
            }
            default: require(false);
        }
        throw std::runtime_error("Unsupported pinned MNN shape expression");
    }

    std::map<int, OpT*> producers;
    std::map<int, int> consumers;
};
}

std::vector<uint8_t> scalarizePinnedUnetBiases(const std::vector<uint8_t>& original) {
    // This size and all payload SHA-256 identities belong to the one supported
    // bundle revision; the application's strict validator runs before native use.
    require(original.size() == 1353144);
    flatbuffers::Verifier verifier(original.data(), original.size());
    require(MNN::VerifyNetBuffer(verifier));
    std::unique_ptr<NetT> net(MNN::GetNet(original.data())->UnPack());
    Graph graph(*net);
    std::vector<std::pair<OpT*, int>> replacements;
    for (const auto& op : net->oplists) {
        if (op->type != MNN::OpType_Softmax) continue;
        require(op->inputIndexes.size() == 1);
        auto& add = graph.producer(op->inputIndexes[0]);
        graph.requireBinary(add, MNN::BinaryOpOperation_ADD);
        auto& multiply = graph.producer(add.inputIndexes[1]);
        graph.requireBinary(multiply, MNN::BinaryOpOperation_MUL);
        auto& fill = graph.producer(multiply.inputIndexes[0]);
        require(fill.type == MNN::OpType_Fill && fill.inputIndexes.size() == 2 && fill.outputIndexes.size() == 1 &&
            graph.isPositiveZero(fill.inputIndexes[1]) && graph.isPositiveZero(multiply.inputIndexes[1]) &&
            graph.producer(multiply.inputIndexes[1]).main.AsBlob()->dims.empty() &&
            graph.consumers.at(fill.outputIndexes[0]) == 1 && graph.consumers.at(multiply.outputIndexes[0]) == 1 &&
            graph.consumers.at(add.outputIndexes[0]) == 1);
        auto& scale = graph.producer(add.inputIndexes[0]);
        graph.requireBinary(scale, MNN::BinaryOpOperation_MUL);
        const auto& matmul = graph.producer(scale.inputIndexes[0]);
        const auto* param = matmul.main.AsMatMul();
        require(matmul.type == MNN::OpType_MatMul && matmul.inputIndexes.size() == 2 && param &&
            param->T == MNN::DataType_DT_FLOAT && !param->transposeA && !param->transposeB);
        const auto& transpose = graph.producer(matmul.inputIndexes[1]);
        const auto* permutation = transpose.main.AsPermute();
        require(transpose.type == MNN::OpType_Permute && transpose.inputIndexes.size() == 1 && permutation &&
            permutation->dims == std::vector<int>({0, 2, 1}));
        const int query = matmul.inputIndexes[0], key = transpose.inputIndexes[0];
        graph.requireRankThreeReshape(query);
        graph.requireRankThreeReshape(key);
        const auto shape = graph.shapeValue(fill.inputIndexes[0], query, key);
        require(!shape.isScalar && shape.values == std::vector<int64_t>({kDimension + 1, kDimension + 2, kDimension + 5}));
        // Retain Add and its FLOAT32 attributes. Scalar +0 broadcasts over the
        // very same QK matrix, including signed-zero/NaN/Inf addition semantics.
        replacements.emplace_back(&add, multiply.inputIndexes[1]);
    }
    require(replacements.size() == 32);
    for (const auto& replacement : replacements) replacement.first->inputIndexes[1] = replacement.second;

    // Drop every now-unreachable producer, including Fill/Mul and shape helpers,
    // before Module::load can materialize their multi-GiB constant tensors.
    std::set<OpT*> live;
    std::vector<int> pending;
    const auto output = std::find(net->tensorName.begin(), net->tensorName.end(), "out_sample");
    require(output != net->tensorName.end());
    pending.push_back(static_cast<int>(output - net->tensorName.begin()));
    while (!pending.empty()) {
        const int tensor = pending.back();
        pending.pop_back();
        auto& producer = graph.producer(tensor);
        if (!live.insert(&producer).second) continue;
        pending.insert(pending.end(), producer.inputIndexes.begin(), producer.inputIndexes.end());
    }
    net->oplists.erase(std::remove_if(net->oplists.begin(), net->oplists.end(),
        [&](const auto& op) { return live.count(op.get()) == 0; }), net->oplists.end());
    flatbuffers::FlatBufferBuilder builder;
    builder.Finish(MNN::Net::Pack(builder, net.get()));
    return {builder.GetBufferPointer(), builder.GetBufferPointer() + builder.GetSize()};
}
}
