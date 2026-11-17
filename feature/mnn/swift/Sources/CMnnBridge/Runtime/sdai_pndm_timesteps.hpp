#pragma once
#include <stdexcept>
#include <vector>

namespace sdai {
// Pinned SD1.5 PNDM: leading spacing, offset1, skip-PRK predictor/corrector.
// The second timestep is evaluated twice; the second pass recomputes the first
// interval before multistep history advances. The one-step profile has no corrector.
inline std::vector<int> pndmTimesteps(int requestedSteps) {
    if (requestedSteps < 1 || requestedSteps > 50) throw std::invalid_argument("Unsupported PNDM step count");
    std::vector<int> result;
    const int ratio = 1000 / requestedSteps;
    for (int index = requestedSteps - 1; index >= 0; --index) result.push_back(1 + index * ratio);
    if (requestedSteps > 1) result.insert(result.begin() + 2, result[1]);
    return result;
}

// Overall work reserves time for text encoding and decoding around native forwards.
inline int pndmProgress(int completedWork, int forwardCount) {
    if (completedWork < 0 || forwardCount < 1) throw std::invalid_argument("Invalid PNDM progress");
    const int value = completedWork * 100 / (forwardCount + 3);
    return value > 100 ? 100 : value;
}
}
