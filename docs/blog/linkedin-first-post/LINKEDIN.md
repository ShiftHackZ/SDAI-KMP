Three years ago, SDAI started as a native Android app for Stable Diffusion workflows. The idea was simple: make mobile AI image generation less tied to one provider, one server, or one way of working.

Since then it has grown through a lot of iterations: self-hosted servers, hosted APIs, local Android generation, galleries, model setup flows, provider-specific controls, open-source releases, and a codebase that survived more than one architectural reality check.

The new milestone is bigger than another release number:

SDAI is now a Kotlin Multiplatform product with a shared core for Android and iOS.

That refactor was not about chasing a trend. It was about keeping the product identity shared while still letting each platform stay native where it matters: file access, background work, storage, model runtimes, and device-specific generation paths.

The part I am most excited about is offline generation.

On Android, SDAI supports local generation paths such as ONNX, MediaPipe, and SDXL through stable-diffusion.cpp. On iOS, it now has local generation work through Core ML and PrismML Bonsai / MLX on supported devices.

That means SDAI is moving closer to the thing I always wanted it to be: an open-source, provider-first mobile AI image generation app where users can choose between cloud, self-hosted, and on-device workflows.

Learn more about the SDAI project:
https://sdai.moroz.cc

I wrote a longer engineering breakdown of the Android Native -> KMP refactor here:
https://moroz.cc/insights/sdai-android-native-to-kmp

#KotlinMultiplatform #AndroidDev #iOSDev #OpenSource #AIImageGeneration
