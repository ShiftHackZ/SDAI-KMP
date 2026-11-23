---
title: "From native Android to Kotlin Multiplatform"
description: "How SDAI moved from a native Android app to a shared Android and iOS codebase while keeping platform-specific generation engines."
date: "2026-06-21"
category: "Engineering"
cover_alt: "SDAI Kotlin Multiplatform engineering announcement artwork."
author: "Dmitriy Moroz"
author_type: "Person"
author_url: "https://moroz.cc/"
---

I started SDAI in 2023 as an Android client for people who already had a Stable Diffusion server. The idea was to connect to it from your phone, send a prompt, and find the result again later. By the time I started working on an iPhone version, the Android app also supported hosted providers and on-device models. Porting the screens would have been only a small part of the work.

## Moving the generation workflow into shared Kotlin

The Android codebase already kept provider requests, storage, and presentation state in separate modules. That gave me a place to start. Provider configuration and generation logic moved into shared Kotlin as Android dependencies were pulled out of the parts both apps would use.

This helps when two providers expose different controls. A self-hosted server might offer a sampler list and batch generation, while a particular hosted endpoint has a much smaller set of options. The shared code tracks what a provider can do, so the generation form can show the controls that apply to the current setup. Both apps can take the user from a prompt to a result in the gallery even when the provider behind that flow changes.

## Where the platforms still differ

Background generation is a good example. The shared app can start work, observe its progress, and request cancellation. Android uses WorkManager to schedule generation. iOS has its own execution rules, so the implementation on iPhone has to work within the time the system grants it. File selection, media export, and sharing also stay with the platform that owns those interactions.

Local inference has a similar boundary. Android keeps its ONNX and MediaPipe providers, along with SDXL through stable-diffusion.cpp. On supported iPhones, Core ML and PrismML Bonsai through MLX have their own model formats and runtime code.

Bonsai made this concrete. Its native runtime can look for model resources inside an extracted folder or a model archive. The shared app asks the runtime to generate an image, then receives progress and the result. The runtime handles file layout and model loading.

I've written up the module boundaries, background execution, and the Swift bridge in more depth. [Read the engineering breakdown of the Android-to-KMP refactor](https://moroz.cc/insights/sdai-android-native-to-kmp).
