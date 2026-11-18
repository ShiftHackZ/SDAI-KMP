#include "scenario.hpp"
#include "core/BufferAllocator.hpp"
#include <cerrno>
#include <cstring>
#include <filesystem>
#include <fstream>
#include <sys/mman.h>
#include <unistd.h>

namespace fs = std::filesystem;
static bool shouldFailReserve = false;
static bool shouldFailResize = false;
static bool shouldFailMap = false;

extern "C" int __real_posix_fallocate(int, off_t, off_t);
extern "C" int __wrap_posix_fallocate(int fd, off_t offset, off_t size) {
    return shouldFailReserve ? ENOSPC : __real_posix_fallocate(fd, offset, size);
}
extern "C" int __real_ftruncate(int, off_t);
extern "C" int __wrap_ftruncate(int fd, off_t size) {
    if (shouldFailResize) { errno = ENOSPC; return -1; }
    return __real_ftruncate(fd, size);
}
extern "C" void* __real_mmap(void*, size_t, int, int, int, off_t);
extern "C" void* __wrap_mmap(void* address, size_t size, int protection, int flags, int fd, off_t offset) {
    if (shouldFailMap) { errno = ENOMEM; return MAP_FAILED; }
    return __real_mmap(address, size, protection, flags, fd, offset);
}

class Workspace {
public:
    Workspace() {
        char pattern[] = "/tmp/sdai-mmap-unit-XXXXXX";
        auto* created = mkdtemp(pattern);
        if (!created) throw std::runtime_error("Cannot create unit directory");
        path = created;
    }
    ~Workspace() {
        shouldFailReserve = shouldFailResize = shouldFailMap = false;
        fs::remove_all(path);
    }
    std::shared_ptr<MNN::BufferAllocator::Allocator> allocator(bool shouldRemove = true) const {
        return MNN::BufferAllocator::Allocator::createMmap(path.c_str(), "unit-", "buffer", shouldRemove, false);
    }
    fs::path path;
};

int main() {
    Scenarios tests;
    tests.run("given private mmap allocation, mapped bytes are written and released, expected reserved backing removed", [] {
        // Given
        Workspace workspace; auto allocator = workspace.allocator();
        // When
        auto chunk = allocator->onAlloc(4097, 4096);
        std::memset(chunk.first, 0x5a, 8192);
        // Then
        expect(fs::file_size(workspace.path / "unit-0.buffer") == 8192, "Backing alignment is wrong");
        expect(static_cast<unsigned char*>(chunk.first)[8191] == 0x5a, "Mapped bytes are inaccessible");
        allocator->onRelease(chunk);
        expect(fs::is_empty(workspace.path), "Released backing leaked");
    });
    tests.run("given private mmap allocation, one chunk is released before another allocation, expected live backing is never reused", [] {
        // Given
        Workspace workspace; auto allocator = workspace.allocator();
        auto first = allocator->onAlloc(4096, 64); auto live = allocator->onAlloc(4096, 64);
        std::memset(live.first, 0x2a, 4096);
        // When
        allocator->onRelease(first); auto next = allocator->onAlloc(4096, 64);
        std::memset(next.first, 0x71, 4096);
        // Then
        expect(fs::exists(workspace.path / "unit-2.buffer"), "Allocation name was recycled");
        expect(static_cast<unsigned char*>(live.first)[0] == 0x2a, "Live chunk was aliased");
        allocator->onRelease(live); allocator->onRelease(next);
        expect(fs::is_empty(workspace.path), "Backing leaked");
    });
    for (const auto& failure : {std::string("reservation"), std::string("creation"), std::string("mapping"), std::string("resize")}) {
        tests.run("given private mmap allocation, " + failure + " fails, expected exception without partial backing or RAM fallback", [&] {
            // Given
            Workspace workspace; auto allocator = workspace.allocator();
            if (failure == "resize") std::ofstream(workspace.path / "unit-0.buffer").put('x');
            shouldFailReserve = failure == "reservation";
            shouldFailResize = failure == "creation" || failure == "resize";
            shouldFailMap = failure == "mapping";
            // When
            auto action = [&] { allocator->onAlloc(4096, 64); };
            // Then
            expectFailure(action, "Failed private allocation did not throw");
            expect(fs::is_empty(workspace.path), "Partial backing retained");
        });
    }
    tests.run("given private mmap allocation, allocator is destroyed with a live chunk, expected backing removed", [] {
        // Given
        Workspace workspace;
        // When
        { auto allocator = workspace.allocator(); allocator->onAlloc(4096, 64); }
        // Then
        expect(fs::is_empty(workspace.path), "Destructor leaked backing");
    });
    tests.run("given retained mmap allocation, chunk is released, expected caller owned cache is preserved", [] {
        // Given
        Workspace workspace; auto allocator = workspace.allocator(false);
        // When
        auto chunk = allocator->onAlloc(4096, 64); allocator->onRelease(chunk);
        // Then
        expect(fs::exists(workspace.path / "unit-0.buffer"), "Non-private cache was removed");
    });
    tests.run("given single buffer growth, replacement allocation throws, expected empty state and no double release", [] {
        // Given
        Workspace workspace; auto allocator = workspace.allocator();
        MNN::SingleBufferWithAllocator buffer; buffer.root = allocator;
        expect(buffer.realloc(4096, 64) == MNN::NO_ERROR, "Initial allocation failed");
        shouldFailReserve = true;
        // When
        auto action = [&] { buffer.realloc(8192, 64); };
        // Then
        expectFailure(action, "Growth failure did not throw");
        expect(buffer.current.first == nullptr && buffer.currentSize == 0, "Released chunk remains referenced");
        buffer.release();
        expect(fs::is_empty(workspace.path), "Failed growth leaked backing");
    });
    return tests.finish();
}
