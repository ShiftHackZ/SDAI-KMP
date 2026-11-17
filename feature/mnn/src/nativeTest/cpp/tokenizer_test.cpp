#include "scenario.hpp"
#include "tokenizer.hpp"
#include "clip_cases.hpp"

int main(int argc, char** argv) {
    if (argc != 2) return 2;
    Scenarios tests;
    for (const auto& sample : clipCases) {
        tests.run("given pinned CLIP tokenization, " + sample.name + ", expected exact reference IDs with EOS padding", [&] {
            // Given
            MNN::DIFFUSION::MtokTokenizer tokenizer(MNN::DIFFUSION::MtokTokenizer::Style::kPair, 49406, 49407);
            expect(tokenizer.load(argv[1]), "Production tokenizer failed to load");
            // When
            const auto ids = tokenizer.encode(sample.prompt, 77);
            // Then
            expect(ids.size() == 154, "CLIP pair length changed");
            expect(std::vector<int>(ids.begin(), ids.begin() + 77) == clipCases.front().ids, "Unconditional tokens differ");
            expect(std::vector<int>(ids.begin() + 77, ids.end()) == sample.ids, "Conditional tokens differ");
        });
    }
    tests.run("given non-CLIP tokenization, another pair profile is loaded, expected original BPE and zero padding", [&] {
        // Given
        MNN::DIFFUSION::MtokTokenizer tokenizer(MNN::DIFFUSION::MtokTokenizer::Style::kPair, 101, 102);
        expect(tokenizer.load(argv[1]), "Tokenizer failed to load");
        // When
        const auto ids = tokenizer.encode("cat", 77);
        // Then
        expect(ids.size() == 154 && ids[77] == 101 && ids[78] == 1481 && ids[79] == 102 && ids[80] == 0, "CLIP policy leaked into another pair profile");
    });
    tests.run("given non-CLIP tokenization, a single sequence profile is loaded, expected original BPE and zero padding", [&] {
        // Given
        MNN::DIFFUSION::MtokTokenizer tokenizer(MNN::DIFFUSION::MtokTokenizer::Style::kSingle, 49406, 49407);
        expect(tokenizer.load(argv[1]), "Tokenizer failed to load");
        // When
        const auto ids = tokenizer.encode("cat", 77);
        // Then
        expect(ids.size() == 77 && ids[0] == 49406 && ids[1] == 1481 && ids[2] == 49407 && ids[3] == 0, "CLIP policy leaked into a single sequence profile");
    });
    return tests.finish();
}
