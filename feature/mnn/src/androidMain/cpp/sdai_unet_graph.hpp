#pragma once

#include <cstdint>
#include <vector>

namespace sdai {
// Rebuilds only the validated SD 1.5 graph in memory. Original graph and external
// weight files retain the SHA-256 identities checked by the bundle validator.
// Throws unless all 32 attention biases have the pinned positive-zero structure.
std::vector<uint8_t> scalarizePinnedUnetBiases(const std::vector<uint8_t>& original);
}
