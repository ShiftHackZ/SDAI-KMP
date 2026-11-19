#!/usr/bin/env python3
"""Publish the nightly announcement and its signed APK through a temporary local Bot API."""

import json
import os
from pathlib import Path
import re
import signal
import socket
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.request


def request(base_url, token, method, payload=None, timeout=30):
    """Call Telegram without retries or exceptions that disclose credential-bearing URLs."""
    data = None if payload is None else json.dumps(payload).encode()
    query = urllib.request.Request(
        f"{base_url}/bot{token}/{method}",
        data=data,
        headers={"Content-Type": "application/json"},
    )
    try:
        with urllib.request.urlopen(query, timeout=timeout) as response:
            result = json.load(response)
    except urllib.error.HTTPError as error:
        raise RuntimeError(f"Telegram {method} failed (HTTP {error.code}).") from None
    except (OSError, ValueError, urllib.error.URLError):
        raise RuntimeError(f"Telegram {method} failed; delivery may be unconfirmed.") from None
    if result.get("ok") is not True:
        raise RuntimeError(f"Telegram {method} was not confirmed.")
    return result["result"]


def cancel(signum, _frame):
    """Unwind the owned server and session when the runner cancels this step."""
    raise SystemExit(128 + signum)


def main():
    """Validate configuration before building, then send both comments and clean up the session."""
    names = (
        "TELEGRAM_BOT_TOKEN",
        "TELEGRAM_CHAT_ID",
        "TELEGRAM_REPLY_TO_MESSAGE_ID",
        "TELEGRAM_API_ID",
        "TELEGRAM_API_HASH",
    )
    settings = {name: os.environ.get(name, "") for name in names}
    if not any(settings.values()):
        print("::notice::Telegram notification skipped: nightly Telegram settings are not configured.")
        return
    if not all(settings.values()):
        raise RuntimeError("Telegram requires the bot token, chat ID, reply ID, API ID and API hash.")
    patterns = {
        "TELEGRAM_CHAT_ID": r"-[0-9]+",
        "TELEGRAM_REPLY_TO_MESSAGE_ID": r"[1-9][0-9]*",
        "TELEGRAM_API_ID": r"[1-9][0-9]*",
        "TELEGRAM_API_HASH": r"[0-9a-fA-F]{32}",
    }
    if any(not re.fullmatch(pattern, settings[name]) for name, pattern in patterns.items()):
        raise RuntimeError("Telegram IDs or API hash have an invalid format.")

    apk = Path(os.environ["NIGHTLY_SIGNED_APK"]).resolve(strict=True)
    apk_size = apk.stat().st_size
    if not apk.is_file() or not 0 < apk_size <= 2_000_000_000:
        raise RuntimeError("The signed nightly APK must be a nonempty file within Telegram's 2 GB limit.")
    runner_temp = Path(os.environ["RUNNER_TEMP"])
    output_dir = runner_temp / "nightly-output"
    text = (output_dir / "telegram.html").read_text()
    announcement = json.loads((output_dir / "announcement.json").read_text())
    binary = runner_temp / "telegram-bot-api/bin/telegram-bot-api"
    if not binary.is_file():
        build_env = {key: value for key, value in os.environ.items() if key not in names}
        subprocess.run(
            ["bash", str(Path(__file__).with_name("build_telegram_bot_api.sh"))],
            env=build_env,
            check=True,
        )

    token = settings["TELEGRAM_BOT_TOKEN"]
    chat_id = int(settings["TELEGRAM_CHAT_ID"])
    reply_id = int(settings["TELEGRAM_REPLY_TO_MESSAGE_ID"])
    # Choose an available loopback port for this owned server.
    with socket.socket() as reservation:
        reservation.bind(("127.0.0.1", 0))
        port = reservation.getsockname()[1]
    base_url = f"http://127.0.0.1:{port}"
    signal.signal(signal.SIGTERM, cancel)
    signal.signal(signal.SIGINT, cancel)
    request("https://api.telegram.org", token, "logOut", {})
    with tempfile.TemporaryDirectory(prefix="sdai-telegram-session-", dir=runner_temp) as session_dir:
        server = subprocess.Popen(
            [str(binary), "--local", "--http-ip-address=127.0.0.1", f"--http-port={port}",
             f"--dir={session_dir}", f"--temp-dir={session_dir}", "--verbosity=0"],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
        try:
            deadline = time.monotonic() + 120
            while True:
                if server.poll() is not None:
                    raise RuntimeError("The local Telegram Bot API server exited before becoming ready.")
                try:
                    request(base_url, token, "getMe", timeout=5)
                    break
                except RuntimeError:
                    if time.monotonic() >= deadline:
                        raise RuntimeError("The local Telegram Bot API server did not become ready.") from None
                    time.sleep(2)

            # Preserve the full text rather than truncate it to Telegram's document caption limit.
            message = request(base_url, token, "sendMessage", {
                "chat_id": chat_id,
                "text": text,
                "parse_mode": "HTML",
                "link_preview_options": {"is_disabled": True},
                "reply_parameters": {"message_id": reply_id, "allow_sending_without_reply": False},
            })
            # Local mode reads the exact signed APK from disk without a second download or memory copy.
            document = request(base_url, token, "sendDocument", {
                "chat_id": chat_id,
                "document": apk.as_uri(),
                "caption": announcement["version"],
                "reply_parameters": {
                    "message_id": message["message_id"],
                    "allow_sending_without_reply": False,
                },
            }, timeout=900)
            if document.get("document", {}).get("file_size") != apk_size:
                raise RuntimeError("Telegram did not confirm the expected APK file size.")
            print("Telegram confirmed the nightly announcement and signed APK attachment.")
        finally:
            has_delivery_failed = sys.exc_info()[0] is not None
            try:
                # Invalidate this session before the disposable runner removes its state.
                request(base_url, token, "logOut", {}, timeout=30)
            except RuntimeError:
                print("::error::Telegram session logout failed.", file=sys.stderr)
                if not has_delivery_failed:
                    raise
            finally:
                server.terminate()
                try:
                    server.wait(timeout=30)
                except subprocess.TimeoutExpired:
                    server.kill()
                    server.wait()


if __name__ == "__main__":
    try:
        main()
    except (RuntimeError, OSError, KeyError, ValueError, subprocess.CalledProcessError) as error:
        # Only deliberate errors are displayed; library diagnostics may contain a token or API hash.
        message = str(error) if isinstance(error, RuntimeError) else type(error).__name__
        print(f"::error::{message}", file=sys.stderr)
        sys.exit(1)
