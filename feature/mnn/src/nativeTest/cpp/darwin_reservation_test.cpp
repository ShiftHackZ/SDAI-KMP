#include "scenario.hpp"
#include "sdai_mmap_reservation.hpp"
#include <cstdlib>
#include <filesystem>
#include <sys/stat.h>

int main() {
    Scenarios tests;
    tests.run("given Darwin mmap backing, all-space reservation succeeds, expected sized real backing", [] {
        // Given
        char path[] = "/tmp/sdai-darwin-reserve-XXXXXX";
        const int descriptor = mkstemp(path);
        expect(descriptor >= 0, "Cannot create backing");
        // When
        const int result = sdai::reserveBacking(descriptor, 65536);
        struct stat info{};
        fstat(descriptor, &info);
        close(descriptor);
        unlink(path);
        // Then
        expect(result == 0 && info.st_size == 65536 && info.st_blocks * 512 >= 65536,
            "Reservation did not allocate complete backing");
    });
    tests.run("given Darwin mmap backing, reservation returns ENOSPC, expected sizing skipped", [] {
        // Given
        bool hasResized = false;
        // When
        const int result = sdai::reserveDarwinBacking(0, 65536,
            [](int, fstore_t*) { errno = ENOSPC; return -1; },
            [&](int, off_t) { hasResized = true; return 0; });
        // Then
        expect(result == ENOSPC && !hasResized, "Reservation failure was hidden");
    });
    tests.run("given Darwin mmap backing, sizing fails after reservation, expected failure propagated", [] {
        // Given
        bool hasReserved = false;
        // When
        const int result = sdai::reserveDarwinBacking(0, 65536,
            [&](int, fstore_t* request) { hasReserved = request->fst_flags == F_ALLOCATEALL; return 0; },
            [](int, off_t) { errno = EIO; return -1; });
        // Then
        expect(hasReserved && result == EIO, "Sizing failure was hidden");
    });
    return tests.finish();
}
