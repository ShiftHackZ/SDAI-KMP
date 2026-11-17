#pragma once

#include <functional>
#include <iostream>
#include <stdexcept>
#include <string>

inline void expect(bool condition, const std::string& message) {
    if (!condition) throw std::runtime_error(message);
}

template <typename Action>
void expectFailure(Action action, const std::string& message) {
    bool hasFailed = false;
    try { action(); } catch (const std::runtime_error&) { hasFailed = true; }
    expect(hasFailed, message);
}

class Scenarios {
public:
    void run(const std::string& name, const std::function<void()>& action) {
        try {
            action();
            ++passed;
            std::cout << "PASS " << name << '\n';
        } catch (const std::exception& error) {
            ++failed;
            std::cerr << "FAIL " << name << ": " << error.what() << '\n';
        }
    }
    int finish() const {
        std::cout << passed << " passed, " << failed << " failed\n";
        return failed == 0 ? 0 : 1;
    }
private:
    int passed = 0;
    int failed = 0;
};
