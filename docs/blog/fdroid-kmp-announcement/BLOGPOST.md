---
title: "SDAI 1.1.1 arrives on F-Droid"
description: "The first Kotlin Multiplatform release of SDAI to reach F-Droid brings the Android FOSS package to version 1.1.1 (208)."
date: "2026-07-19"
category: "Announcement"
cover_alt: "SDAI F-Droid announcement artwork."
---

SDAI 1.1.1 (build 208) is available through F-Droid. It's the first Android FOSS package from the Kotlin Multiplatform line to reach the official F-Droid repository. The work that brought SDAI to iPhone also gave the Android app a shared codebase for provider setup and image generation.

## What changed in the F-Droid listing

The previous listing was on the 0.6.8 release line. The move to 1.1.1 brings F-Droid users the newer provider setup screens, which let you search and filter providers before configuring one. Android's Local SDXL option, the on-device benchmark, and the newer storage and network usage screens also come from this release line. The [SDAI 1.1.0 release post](/blog/1.1.0-release/) explains what those features do.

Version 1.1.1 itself addresses an issue with building the app reproducibly for F-Droid. The fix is listed in the [1.1.1 release notes](https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/1.1.1).

F-Droid distributes the Android FOSS edition. If that's where you installed SDAI, look for the update in the F-Droid client. New users can start from the [official SDAI listing on F-Droid](https://f-droid.org/packages/com.shifthackz.aisdv1.app.foss/) and check the version details there.

## Which devices can install 1.1.1?

The F-Droid build announced here requires Android 7.0 or later and support for the `arm64-v8a` ABI. Both are required. A device running a recent Android version can still be incompatible if its system supports only `armeabi-v7a`, `x86`, or `x86_64`. If the update doesn't appear, check the device's supported ABIs as well as its Android version.
