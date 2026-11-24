---
title: "SDAI 1.3.0: MNN, Easy Diffusion, OpenAI-compatible, and ChatGPT OAuth"
description: "See what’s new in SDAI 1.3.0 for Android and iPhone, including Stable Diffusion 1.5 with MNN, Easy Diffusion, ChatGPT OAuth, Civitai publishing, and backups."
date: "2026-11-24"
category: "Release"
cover_alt: "SDAI News cover highlighting MNN, Easy Diffusion, OpenAI-compatible, ChatGPT OAuth, Civitai publishing, and selective backups beside a phone displaying a mountain scene."
---

SDAI 1.3.0 is available on the App Store and Google Play. You can now generate images offline with MNN on supported phones, publish gallery images to Civitai, and choose which parts of your setup go into a backup. The release also adds Easy Diffusion, OpenAI-compatible servers, and ChatGPT OAuth as generation options.

[Update on the App Store](https://apps.apple.com/us/app/sdai-ai-image-generator/id6778314183) · [Update on Google Play](https://play.google.com/store/apps/details?id=com.shifthackz.aisdv1.app)

## Generate on your phone with MNN

Choose **MNN** in provider setup, download the **SD 1.5 General** model, then select it and connect. Once the model is installed, you can run Stable Diffusion 1.5 offline. Supported arm64 Android devices can use CPU or OpenCL. Supported iPhones running iOS 17 or later can use CPU or Metal.

MNN generates one 512 × 512 image from a text prompt per request. The model download is about 1.15 GB. Check the [MNN setup guide](/mnn.html) for storage and memory requirements before you download it.

## Connect to Easy Diffusion, OpenAI-compatible servers, and ChatGPT

- **Easy Diffusion** connects to your Easy Diffusion v3 server over the network. It supports text-to-image and image-to-image generation with compatible SD 1.5 models.
- **OpenAI-compatible** connects to a compatible image server or proxy. Enter its API prefix and model ID, and supply an API key if required. [OpenAI-compatible setup guide](/openai-compatible.html)
- **ChatGPT OAuth** uses device-code sign-in for image generation through ChatGPT. Availability and usage limits depend on your account. [ChatGPT OAuth setup guide](/openai-oauth.html)

## Publish from your gallery with Civitai

Open **Settings → Integrations → Civitai** and connect using OAuth or an API key. To publish a generated image, open it in the gallery and tap the Civitai action. SDAI saves the public post link in the image details and in your upload history.

Automatic uploads are off by default. If you turn them on, SDAI publishes new generations with their recorded metadata. This can include your prompt, generation settings, provider, model, device, and operating system. See the [privacy policy](/privacy.html) for what SDAI sends to Civitai.

## Choose what goes into a backup

In **Settings → Backup and restore**, select the sections you want to export. You can include provider configurations, gallery and generation history, app settings, network statistics, and Civitai upload history. You can restore a backup during setup on a fresh installation and review any provider conflicts before replacing configurations.

Backups that include provider configurations may contain unencrypted credentials. Keep those files somewhere private.

## Download models and follow generation progress

Use **Download all** to queue the missing models in a local catalog. You can see which model is downloading and cancel the batch. This release also improves catalog loading, retry feedback after a failed download, and model selection when switching providers.

The generation screen shows elapsed time and, when the provider reports progress, a percentage or step count. Live CPU and RAM charts show resource use during model loading and generation. If you enable screen dimming, SDAI lowers brightness during a foreground run and restores it when you touch the screen or when the run ends.

Open a result in the gallery to see the details recorded for that run, such as the provider, acceleration mode, generation time, device, and operating system.

## Interface updates and fixes

- Refined native iPhone controls, toolbars, tab transitions, Back navigation, and gallery actions.
- Added prompt-clear buttons, access to saved images from the image picker, and contextual help.
- Language changes take effect immediately. Sampling steps and CFG scale are remembered after restarting the app.
- Added a configurable generation timeout and improved setup for servers with large LoRA libraries or without hypernetwork endpoints.
- Fixed sampler-list recovery and web UI seed compatibility. Android also received fixes for image exports, ONNX compatibility with NNAPI, and startup stability.
- Optional store update prompts can point you to a newer release when one is available.
