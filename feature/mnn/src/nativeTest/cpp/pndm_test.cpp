#include "scenario.hpp"
#include "sdai_pndm_timesteps.hpp"

int main() {
    Scenarios tests;
    const std::vector<std::pair<int, std::vector<int>>> references = {
        {1, {1}},
        {14, {924,853,853,782,711,640,569,498,427,356,285,214,143,72,1}},
        {20, {951,901,901,851,801,751,701,651,601,551,501,451,401,351,301,251,201,151,101,51,1}},
        {50, {981,961,961,941,921,901,881,861,841,821,801,781,761,741,721,701,681,661,641,621,601,581,561,541,521,501,481,461,441,421,401,381,361,341,321,301,281,261,241,221,201,181,161,141,121,101,81,61,41,21,1}}
    };
    for (const auto& sample : references) {
        tests.run("given pinned PNDM schedule, " + std::to_string(sample.first) + " steps are requested, expected reference predictor-corrector timesteps", [&] {
            // Given
            const auto expected = sample.second;
            // When
            const auto actual = sdai::pndmTimesteps(sample.first);
            // Then
            expect(actual == expected, "PNDM model timestep and latent phase diverged");
        });
    }
    tests.run("given PNDM progress, corrected forward schedule runs, expected monotonic overall percentage below completion", [&] {
        // Given
        const auto count = static_cast<int>(sdai::pndmTimesteps(20).size());
        int previous = 0;
        // When
        for (int work = 0; work <= count + 3; ++work) {
            const int value = sdai::pndmProgress(work, count);
            // Then
            expect(value >= previous && value >= 0 && value <= 100, "Invalid progress percentage");
            previous = value;
        }
        expect(previous == 100, "Completion percentage missing");
    });
    return tests.finish();
}
