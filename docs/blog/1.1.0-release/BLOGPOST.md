---
title: "SDAI 1.1.0: More ways to generate locally"
description: "Version 1.1.0 adds Android SDXL, Core ML and Bonsai on iPhone, an on-device benchmark, and two hosted providers."
date: "2026-06-19"
category: "Release"
cover_alt: "SDAI 1.1.0 release artwork."
---

SDAI 1.1.0 (build 204) brings SDXL to Android and adds two ways to generate images on an iPhone. If you've put off trying local generation because you weren't sure your phone could handle it, this release also gives you a way to check before committing to a heavier run.

## Start with the benchmark

The on-device benchmark saves its results and recommends a starting point for supported local providers. A model that runs well on one phone can still be too demanding on another. If a run struggles, try a smaller or quantized model where the provider supports one. The [FAQ covers device fit and local generation](/faq.html).

On Android, Local SDXL runs text-to-image generation through stable-diffusion.cpp. You can choose an SDXL model from the catalog or select a compatible local model. On supported iPhones, Silicon Diffusion Core ML runs compatible Stable Diffusion models, and the PrismML Bonsai Image 4B integration adds a beta option backed by Apple's MLX framework.

## Two more hosted providers

Fal.ai supports compatible text-to-image and image-to-image endpoints. ArliAI adds a hosted option that uses an SDNext-compatible API. Depending on the endpoint, you can discover checkpoints, pick a sampler, set sampling steps and guidance, add a negative prompt, or run a batch.

The provider picker now has search, sorting, and filters for provider type, readiness, and feature tags. If you know you need image-to-image, you can narrow the list before you open a provider's setup screen.

## See what the app is using

The new storage screens break down space used by downloaded models, cached files, and the gallery. Network usage shows traffic from model downloads, configuration downloads, and generation requests. If free space drops after trying a model, or a provider uses more data than you expected, Settings now gives you a place to check.

On iPhone, we also improved gallery loading and how the app handles background work during longer generations. The Android APKs and full changelog are on the [1.1.0 release on GitHub](https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/1.1.0).
