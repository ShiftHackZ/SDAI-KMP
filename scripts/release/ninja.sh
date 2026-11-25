#!/usr/bin/env bash
# AGP invokes Ninja directly, so a CMake environment limit alone is insufficient.
set -euo pipefail
for arg in "$@"; do
    if [[ "$arg" == -t ]]; then
        exec "${SDAI_REAL_NINJA:?Pinned Ninja path required}" "$@"
    fi
done
native_workers=2
if [[ "${SDAI_RELEASE_FLAVOR:-}" == foss ]]; then
    native_workers="${SDAI_NATIVE_WORKERS:?FOSS native worker count required}"
fi
exec "${SDAI_REAL_NINJA:?Pinned Ninja path required}" "$@" "-j$native_workers"
