#include "scenario.hpp"
#include "sdai_unet_graph.hpp"
#include "MNN_generated.h"
#include <algorithm>
#include <cmath>
#include <fstream>
#include <iterator>
#include <set>

using MNN::NetT;
using MNN::OpT;

static std::vector<uint8_t> pack(const NetT& net) {
    flatbuffers::FlatBufferBuilder builder;
    builder.Finish(MNN::Net::Pack(builder, &net));
    expect(builder.GetSize() <= 1353144, "Mutation exceeds pinned envelope");
    std::vector<uint8_t> bytes(builder.GetBufferPointer(), builder.GetBufferPointer() + builder.GetSize());
    bytes.resize(1353144);
    return bytes;
}

static OpT& producer(NetT& net, int tensor) {
    for (auto& op : net.oplists) {
        if (std::find(op->outputIndexes.begin(), op->outputIndexes.end(), tensor) != op->outputIndexes.end()) return *op;
    }
    throw std::runtime_error("Missing fixture producer");
}

static OpT& first(NetT& net, MNN::OpType type) {
    for (auto& op : net.oplists) if (op->type == type) return *op;
    throw std::runtime_error("Missing fixture operation");
}

int main(int argc, char** argv) {
    if (argc != 2) return 2;
    std::ifstream file(argv[1], std::ios::binary);
    const std::vector<uint8_t> original((std::istreambuf_iterator<char>(file)), {});
    Scenarios tests;
    tests.run("given UNet scalarization, pinned graph is adapted, expected intact inputs and 32 scalar positive-zero attention adds", [&] {
        // Given
        const auto before = original;
        std::unique_ptr<NetT> input(MNN::GetNet(original.data())->UnPack());
        std::set<int> denseBiases;
        for (auto& op : input->oplists) {
            if (op->type != MNN::OpType_Softmax) continue;
            auto& add = producer(*input, op->inputIndexes.at(0));
            auto& multiply = producer(*input, add.inputIndexes.at(1));
            denseBiases.insert(multiply.inputIndexes.at(0));
        }
        // When
        const auto bytes = sdai::scalarizePinnedUnetBiases(original);
        std::unique_ptr<NetT> net(MNN::GetNet(bytes.data())->UnPack());
        int count = 0;
        // Then
        expect(original == before, "Original graph was changed");
        expect(bytes.size() < original.size(), "Unreachable bias helpers remain");
        for (auto& op : net->oplists) {
            for (int tensor : op->outputIndexes) expect(denseBiases.count(tensor) == 0, "Dense attention Fill remains");
            if (op->type != MNN::OpType_Softmax) continue;
            auto& add = producer(*net, op->inputIndexes.at(0));
            auto* binary = add.main.AsBinaryOp();
            expect(binary && binary->opType == MNN::BinaryOpOperation_ADD && binary->activationType == 0 && binary->T == MNN::DataType_DT_FLOAT, "Attention addition semantics changed");
            auto* scalar = producer(*net, add.inputIndexes.at(1)).main.AsBlob();
            expect(scalar && scalar->dims.empty() && scalar->float32s == std::vector<float>{0.f} && !std::signbit(scalar->float32s[0]), "Not scalar positive zero");
            ++count;
        }
        expect(count == 32, "Attention chain lost");
        for (const auto& name : {"sample", "timestep", "encoder_hidden_states", "out_sample"}) {
            auto found = std::find(net->tensorName.begin(), net->tensorName.end(), name);
            expect(found != net->tensorName.end(), "Graph signature lost");
            producer(*net, static_cast<int>(found - net->tensorName.begin()));
        }
    });
    tests.run("given Metal attention fusion, pinned graph is adapted, expected 32 noncausal bounded operators and unchanged CPU graph", [&] {
        // Given
        const auto before = original;
        // When
        const auto bytes = sdai::boundPinnedMetalAttention(original);
        std::unique_ptr<NetT> net(MNN::GetNet(bytes.data())->UnPack());
        int attentionCount = 0, softmaxCount = 0;
        // Then
        for (const auto& op : net->oplists) {
            if (op->type == MNN::OpType_Softmax) ++softmaxCount;
            if (op->type != MNN::OpType_Attention) continue;
            ++attentionCount;
            const auto* param = op->main.AsAttentionParam();
            expect(param && !param->kv_cache && !param->output_c4 && param->mhq_quant.empty(), "Attention semantics changed");
            expect(op->inputIndexes.size() == 3, "QKV inputs missing");
            for (int input : op->inputIndexes) {
                const auto& expand = producer(*net, input);
                expect(expand.type == MNN::OpType_Unsqueeze && expand.main.AsSqueezeParam() &&
                    expand.main.AsSqueezeParam()->squeezeDims == std::vector<int>{2}, "Flattened heads changed layout");
            }
        }
        expect(attentionCount == 32 && softmaxCount == 0, "Dense attention chain remains");
        expect(original == before, "Immutable graph bytes changed");
        const auto cpu = sdai::scalarizePinnedUnetBiases(original);
        std::unique_ptr<NetT> cpuNet(MNN::GetNet(cpu.data())->UnPack());
        for (const auto& op : cpuNet->oplists) expect(op->type != MNN::OpType_Attention, "Metal policy leaked into CPU graph");
    });
    auto rejectMetal = [&](const std::string& condition, const std::function<void(NetT&)>& mutate) {
        tests.run("given Metal attention fusion, " + condition + ", expected strict rejection", [&] {
            // Given
            std::unique_ptr<NetT> net(MNN::GetNet(original.data())->UnPack());
            mutate(*net);
            const auto bytes = pack(*net);
            // When
            const auto action = [&] { sdai::boundPinnedMetalAttention(bytes); };
            // Then
            expectFailure(action, "Changed attention semantics accepted");
        });
    };
    rejectMetal("softmax axis changes", [&](NetT& net) { first(net, MNN::OpType_Softmax).main.AsAxis()->axis = 1; });
    rejectMetal("query scaling changes", [&](NetT& net) {
        auto& add = producer(net, first(net, MNN::OpType_Softmax).inputIndexes[0]);
        auto& scale = producer(net, add.inputIndexes[0]);
        producer(net, scale.inputIndexes[1]).main.AsBlob()->float32s[0] *= 0.5f;
    });
    rejectMetal("value matmul is transposed", [&](NetT& net) {
        const int probability = first(net, MNN::OpType_Softmax).outputIndexes[0];
        for (auto& op : net.oplists) if (op->type == MNN::OpType_MatMul && op->inputIndexes[0] == probability)
            op->main.AsMatMul()->transposeB = true;
    });
    rejectMetal("dense query-key tensor has another consumer", [&](NetT& net) {
        auto& add = producer(net, first(net, MNN::OpType_Softmax).inputIndexes[0]);
        const auto& scale = producer(net, add.inputIndexes[0]);
        net.oplists.back()->inputIndexes.push_back(scale.inputIndexes[0]);
    });
    rejectMetal("probabilities have another consumer", [&](NetT& net) {
        net.oplists.back()->inputIndexes.push_back(first(net, MNN::OpType_Softmax).outputIndexes[0]);
    });
    rejectMetal("query head permutation changes", [&](NetT& net) {
        auto& add = producer(net, first(net, MNN::OpType_Softmax).inputIndexes[0]);
        auto& scale = producer(net, add.inputIndexes[0]);
        auto& qk = producer(net, scale.inputIndexes[0]);
        auto& reshape = producer(net, qk.inputIndexes[0]);
        producer(net, reshape.inputIndexes[0]).main.AsPermute()->dims = {0, 1, 2, 3};
    });
    auto reject = [&](const std::string& name, const std::function<void(NetT&)>& mutate) {
        tests.run(name, [&] {
            // Given
            std::unique_ptr<NetT> net(MNN::GetNet(original.data())->UnPack());
            mutate(*net);
            const auto bytes = pack(*net);
            // When
            auto action = [&] { sdai::scalarizePinnedUnetBiases(bytes); };
            // Then
            expectFailure(action, "Unsafe graph accepted");
        });
    };
    tests.run("given UNet scalarization, graph size changes, expected rejection", [&] {
        // Given
        auto bytes = original; bytes.pop_back();
        // When
        auto action = [&] { sdai::scalarizePinnedUnetBiases(bytes); };
        // Then
        expectFailure(action, "Wrong size accepted");
    });
    tests.run("given UNet scalarization, FlatBuffer is corrupt, expected rejection", [&] {
        // Given
        auto bytes = original; std::fill(bytes.begin(), bytes.begin() + 4, 0xff);
        // When
        auto action = [&] { sdai::scalarizePinnedUnetBiases(bytes); };
        // Then
        expectFailure(action, "Corrupt graph accepted");
    });
    reject("given UNet scalarization, a tensor index is outside graph, expected rejection", [](NetT& net) { first(net, MNN::OpType_Softmax).inputIndexes[0] = net.tensorName.size(); });
    reject("given UNet scalarization, two operations produce one tensor, expected rejection", [](NetT& net) { net.oplists[1]->outputIndexes = net.oplists[0]->outputIndexes; });
    reject("given UNet scalarization, output name is missing, expected rejection", [](NetT& net) { for (auto& name : net.tensorName) if (name == "out_sample") name = "bad_sample"; });
    reject("given UNet scalarization, one attention chain is missing, expected rejection", [](NetT& net) { first(net, MNN::OpType_Softmax).type = MNN::OpType_TanH; });
    reject("given UNet scalarization, attention add has activation, expected rejection", [](NetT& net) { auto& softmax = first(net, MNN::OpType_Softmax); producer(net, softmax.inputIndexes[0]).main.AsBinaryOp()->activationType = 1; });
    auto mask = [](NetT& net) -> OpT& { auto& add = producer(net, first(net, MNN::OpType_Softmax).inputIndexes[0]); return producer(net, add.inputIndexes[1]); };
    reject("given UNet scalarization, mask scalar is negative zero, expected rejection", [&](NetT& net) { auto& mul = mask(net); producer(net, mul.inputIndexes[1]).main.AsBlob()->float32s[0] = -0.f; });
    reject("given UNet scalarization, mask scalar is externally stored, expected rejection", [&](NetT& net) { auto& mul = mask(net); producer(net, mul.inputIndexes[1]).main.AsBlob()->external = {0, 4}; });
    reject("given UNet scalarization, mask multiplication is integer, expected rejection", [&](NetT& net) { mask(net).main.AsBinaryOp()->T = MNN::DataType_DT_INT32; });
    reject("given UNet scalarization, mask has another consumer, expected rejection", [&](NetT& net) { auto& mul = mask(net); net.oplists.back()->inputIndexes.push_back(mul.outputIndexes[0]); });
    reject("given UNet scalarization, attention matrix is transposed, expected rejection", [](NetT& net) { auto& add = producer(net, first(net, MNN::OpType_Softmax).inputIndexes[0]); auto& scale = producer(net, add.inputIndexes[0]); producer(net, scale.inputIndexes[0]).main.AsMatMul()->transposeA = true; });
    reject("given UNet scalarization, key permutation changes dimensions, expected rejection", [](NetT& net) { auto& add = producer(net, first(net, MNN::OpType_Softmax).inputIndexes[0]); auto& scale = producer(net, add.inputIndexes[0]); auto& matmul = producer(net, scale.inputIndexes[0]); producer(net, matmul.inputIndexes[1]).main.AsPermute()->dims = {0, 1, 2}; });
    reject("given UNet scalarization, bias shape is unrelated to query and key, expected rejection", [&](NetT& net) { auto& fill = producer(net, mask(net).inputIndexes[0]); fill.inputIndexes[0] = mask(net).inputIndexes[1]; });
    return tests.finish();
}
