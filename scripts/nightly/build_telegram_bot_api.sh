#!/usr/bin/env bash
# Build only the pinned upstream executable; bot sessions never enter this directory.
set -euo pipefail

: "${RUNNER_TEMP:?Missing runner temporary directory.}"
: "${TELEGRAM_BOT_API_REVISION:?Missing Telegram Bot API source revision.}"
if [[ ! "${TELEGRAM_BOT_API_REVISION}" =~ ^[0-9a-f]{40}$ ]]; then
  echo "Telegram Bot API revision must be a full commit SHA." >&2
  exit 1
fi

source_dir="${RUNNER_TEMP}/telegram-bot-api/source"
binary_dir="${RUNNER_TEMP}/telegram-bot-api/bin"
sudo apt-get update
sudo apt-get install --yes --no-install-recommends cmake g++ make gperf libssl-dev zlib1g-dev
git init "${source_dir}"
git -C "${source_dir}" fetch --depth 1 https://github.com/tdlib/telegram-bot-api.git "${TELEGRAM_BOT_API_REVISION}"
git -C "${source_dir}" checkout --detach FETCH_HEAD
git -C "${source_dir}" submodule update --init --recursive --depth 1
cmake -S "${source_dir}" -B "${source_dir}/build" -DCMAKE_BUILD_TYPE=Release
cmake --build "${source_dir}/build" --target telegram-bot-api --parallel 2
install -d "${binary_dir}"
install -m 755 "${source_dir}/build/telegram-bot-api" "${binary_dir}/telegram-bot-api"
