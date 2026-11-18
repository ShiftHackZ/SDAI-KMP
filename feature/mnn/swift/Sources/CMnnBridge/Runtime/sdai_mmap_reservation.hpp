#pragma once
#include <cerrno>
#include <cstddef>
#include <fcntl.h>
#include <unistd.h>

namespace sdai {
#if defined(__APPLE__)
// Keep allocation and sizing separate so no sparse mapping can survive a failed reservation.
template<class Allocate, class Resize>
int reserveDarwinBacking(int file, size_t size, Allocate allocate, Resize resize) {
    fstore_t reservation{};
    reservation.fst_flags = F_ALLOCATEALL;
    reservation.fst_posmode = F_PEOFPOSMODE;
    reservation.fst_length = static_cast<off_t>(size);
    if (allocate(file, &reservation) == -1) return errno;
    if (resize(file, static_cast<off_t>(size)) == -1) return errno;
    return 0;
}
#endif
inline int reserveBacking(int file, size_t size) {
#if defined(__APPLE__)
    return reserveDarwinBacking(file, size,
        [](int descriptor, fstore_t* reservation) { return fcntl(descriptor, F_PREALLOCATE, reservation); },
        [](int descriptor, off_t length) { return ftruncate(descriptor, length); });
#else
    return posix_fallocate(file, 0, static_cast<off_t>(size));
#endif
}
}
