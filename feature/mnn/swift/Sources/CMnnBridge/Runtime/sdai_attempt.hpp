#pragma once
#include <string>
#include <stdexcept>

namespace sdai {
// The executing thread owns configuration until all native stages return.
inline thread_local std::string privateCache;
inline const char* attemptCachePath() {
    if (privateCache.empty()) throw std::runtime_error("Missing MNN attempt workspace");
    return privateCache.c_str();
}
class AttemptConfiguration {
public:
    explicit AttemptConfiguration(const std::string& cache) { privateCache = cache; }
    ~AttemptConfiguration() { privateCache.clear(); }
    AttemptConfiguration(const AttemptConfiguration&) = delete;
    AttemptConfiguration& operator=(const AttemptConfiguration&) = delete;
};
}
