#!/usr/bin/env bash
# Run FOSS builds under distinct real users/hostnames on disposable Linux runners.
set -euo pipefail
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source_dir="$(cd "${1:?Source required}" && pwd)"
flavor="${2:?Flavor required}"
format="${3:?Format required}"
output_dir="${4:?Output required}"
sha="${5:?Source SHA required}"
profile="${6:?Profile required}"
if [[ "$flavor" != foss ]]; then
    exec bash "$script_dir/build.sh" "$source_dir" "$flavor" "$format" "$output_dir" "$sha"
fi
case "$profile" in baseline|varied) ;; *) echo 'Invalid FOSS build profile' >&2; exit 1 ;; esac
for required_variable in SDAI_TOOLCHAIN_DIR ANDROID_HOME JAVA_HOME SDAI_GRADLE GRADLE_USER_HOME RUNNER_TEMP; do
    [[ -n "${!required_variable:-}" ]] || { printf 'Missing build environment: %s\n' "$required_variable" >&2; exit 1; }
done
build_user="sdai-$profile"
sudo useradd --create-home --user-group --shell /bin/bash "$build_user"
printf '127.0.1.1 sdai-build-%s\n' "$profile" | sudo tee -a /etc/hosts >/dev/null
sudo hostname "sdai-build-$profile"
sudo chown -R "$build_user:$build_user" "$source_dir" "$SDAI_TOOLCHAIN_DIR" "$ANDROID_HOME"
build_temp="$RUNNER_TEMP/build-$profile"
sudo install -d -o "$build_user" -g "$build_user" "$output_dir" "$build_temp"
# Runner workspace ancestors may be private to the runner identity. Grant this
# build identity traversal, and read/execute access only to the public CI tools.
sudo setfacl -R -m "u:$build_user:rX" "$script_dir"
for required_path in "$script_dir" "$source_dir" "$SDAI_TOOLCHAIN_DIR" "$ANDROID_HOME" "$output_dir" "$build_temp"; do
    access_parent="$(dirname "$required_path")"
    while [[ "$access_parent" != / ]]; do
        sudo setfacl -m "u:$build_user:x" "$access_parent"
        access_parent="$(dirname "$access_parent")"
    done
done
# Restore artifact ownership even on failure so Actions can retain private build evidence.
runner_owner="$(id -u):$(id -g)"
volume="$RUNNER_TEMP/source-volume-$profile"
image="$RUNNER_TEMP/source-$profile.ext4"
cleanup() {
    build_status=$?
    trap - EXIT
    sudo chown -R "$runner_owner" "$output_dir" || build_status=1
    if mountpoint -q "$volume"; then
        # Never remove an image while its owned mount is still active.
        sudo umount "$volume" || exit 1
    fi
    [[ ! -d "$volume" ]] || rmdir "$volume" || build_status=1
    [[ ! -f "$image" ]] || rm -- "$image" || build_status=1
    exit "$build_status"
}
[[ ! -e "$volume" && ! -e "$image" ]] || { echo 'Build volume already exists' >&2; exit 1; }
trap cleanup EXIT
# Use the same native filesystem setup for both builds. The sparse image consumes
# only written blocks; reserve host space for dependency caches and final artifacts.
available_bytes="$(df --output=avail -B1 "$build_temp" | tail -n 1)"
image_bytes=$((available_bytes - 8 * 1024 * 1024 * 1024))
(( image_bytes <= 32 * 1024 * 1024 * 1024 )) || image_bytes=$((32 * 1024 * 1024 * 1024))
(( image_bytes >= 8 * 1024 * 1024 * 1024 )) || { echo 'Insufficient space for native build volume' >&2; exit 1; }
truncate -s "$image_bytes" "$image"
mkfs.ext4 -q -F -m 0 -E lazy_itable_init=0,lazy_journal_init=0 "$image"
mkdir "$volume"
sudo mount -o loop,nodev,nosuid "$image" "$volume"
sudo chown "$build_user:$build_user" "$volume"
# PAM can inject the runner's XDG paths even with sudo --set-home. Clear those
# paths after sudo so Android and other tools use the executing user's home.
build_environment=(env
    -u XDG_CONFIG_HOME -u XDG_CACHE_HOME -u XDG_DATA_HOME -u XDG_STATE_HOME -u XDG_RUNTIME_DIR
    -u ANDROID_USER_HOME -u ANDROID_PREFS_ROOT -u ANDROID_SDK_HOME
    PATH="$PATH" JAVA_HOME="$JAVA_HOME"
    ANDROID_HOME="$ANDROID_HOME" ANDROID_SDK_ROOT="$ANDROID_HOME"
    GRADLE_USER_HOME="$GRADLE_USER_HOME" SDAI_GRADLE="$SDAI_GRADLE"
    SDAI_TOOLCHAIN_DIR="$SDAI_TOOLCHAIN_DIR" RUNNER_TEMP="$build_temp"
    SDAI_REPRO_PROFILE="$profile")
sudo --user "$build_user" --set-home "${build_environment[@]}" bash -c \
    'set -e
     test -r "$1/build.sh"
     test -r "$1/prepare_rebuild.py"
     test -x "$2/bin/java"
     test -x "$3"
     mkdir -p "$HOME/.android" "$HOME/.config" "$HOME/.cache"
     test -w "$HOME/.android"' \
    build-access-check "$script_dir" "$JAVA_HOME" "$SDAI_GRADLE"
prepared_source="$volume/source"
build_command=(bash "$script_dir/build.sh" "$prepared_source" "$flavor" "$format" "$output_dir" "$sha")
sudo --user "$build_user" --set-home "${build_environment[@]}" \
    python3 "$script_dir/prepare_rebuild.py" "$source_dir" "$prepared_source" "$profile" -- "${build_command[@]}"
