#!/usr/bin/env python3
"""Capture the real iOS app through XCTest, using simctl for the selected framebuffer.

The Swift UI driver acknowledges each screen via its own temporary marker. This
avoids XCTest's black/inactive-display screenshot path on multi-display devices.
All projects, runners and logs live in a temporary directory outside Git.
"""
import argparse
import glob
import json
import pathlib
import shutil
import subprocess
import tempfile
import time


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("config", type=pathlib.Path)
    args = parser.parse_args()
    config = json.loads(args.config.read_text())
    devices = json.loads(subprocess.check_output(["xcrun", "simctl", "list", "devices", "--json"], text=True))["devices"]
    selected = next(device for group in devices.values() for device in group if device["udid"] == config["device"])
    if selected["state"] != "Booted":
        subprocess.run(["xcrun", "simctl", "boot", config["device"]], check=True)
    if config.get("shouldUseLightTheme", True):
        subprocess.run(["xcrun", "simctl", "ui", config["device"], "appearance", "light"], check=True)
    subprocess.run(["xcrun", "simctl", "status_bar", config["device"], "override", "--time", "09:41", "--wifiBars", "3", "--cellularBars", "4", "--batteryState", "charged", "--batteryLevel", "100"], check=True)
    tools = pathlib.Path(__file__).resolve().parent
    work = pathlib.Path(tempfile.mkdtemp(prefix="sdai-store-capture-"))
    project = work / "StoreCapture.xcodeproj"
    project.mkdir()
    shutil.copyfile(tools / "ios-capture.pbxproj", project / "project.pbxproj")
    source = (tools / "capture-ios.swift").read_text()
    source += '\nlet captureConfigJSON = #"' + json.dumps(config, ensure_ascii=False) + '"#\n'
    (work / "Capture.swift").write_text(source)
    if config.get("shouldReset"):
        installed = subprocess.check_output(["xcrun", "simctl", "get_app_container", config["device"], config["bundle"], "app"], text=True).strip()
        app_copy = work / "CaptureApp.app"
        shutil.copytree(installed, app_copy, symlinks=True)
        subprocess.run(["xcrun", "simctl", "uninstall", config["device"], config["bundle"]], check=True)
        subprocess.run(["xcrun", "simctl", "install", config["device"], str(app_copy)], check=True)
    output = pathlib.Path(config["output"])
    output.mkdir(parents=True, exist_ok=True)
    log = work / "capture.log"
    derived = pathlib.Path(tempfile.gettempdir()) / "sdai-store-capture-derived" / config["device"]
    command = ["xcodebuild", "-project", str(project), "-scheme", "StoreCapture", "-destination", "platform=iOS Simulator,id=" + config["device"], "-derivedDataPath", str(derived), "-parallel-testing-enabled", "NO", "test"]
    marker_glob = str(pathlib.Path.home() / "Library/Developer/CoreSimulator/Devices" / config["device"] / "data/Containers/Data/Application/*/tmp/sdai-store-capture.ready")
    for marker in glob.glob(marker_glob):
        pathlib.Path(marker).unlink()
    captured = set()
    try:
        with log.open("w") as stream:
            process = subprocess.Popen(command, stdout=stream, stderr=subprocess.STDOUT)
            try:
                while process.poll() is None:
                    for marker in glob.glob(marker_glob):
                        path = pathlib.Path(marker)
                        name = path.read_text().strip()
                        if name not in {"01-create", "02-controls", "03-providers", "04-gallery", "05-settings", "06-open", "settings-light"}:
                            raise RuntimeError("Unknown capture marker")
                        subprocess.run(["xcrun", "simctl", "io", config["device"], "screenshot", "--display=" + str(config.get("display", 1)), str(output / (name + ".png"))], check=True)
                        captured.add(name)
                        path.unlink()
                    time.sleep(0.2)
            finally:
                if process.poll() is None:
                    process.terminate()
                process.wait()
        if process.returncode != 0:
            raise RuntimeError("XCTest capture failed; see " + str(log))
        if len(captured) != (2 if config.get("section") == "appearance" else 7):
            raise RuntimeError("Incomplete capture; see " + str(log))
        print("Captured real app screens:", output)
    finally:
        subprocess.run(["xcrun", "simctl", "uninstall", config["device"], "com.shifthackz.sdai.storecapture.xctrunner"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        print("Capture log:", log)


if __name__ == "__main__":
    main()
