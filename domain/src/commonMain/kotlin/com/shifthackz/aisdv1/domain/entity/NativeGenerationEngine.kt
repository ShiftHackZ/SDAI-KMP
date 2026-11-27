package com.shifthackz.aisdv1.domain.entity

/**
 * Native execution family used for request admission and lifecycle reporting.
 * Android isolates these engines in a child process; Apple MNN uses a serial in-process owner.
 *
 * @author Dmitriy Moroz
 */
enum class NativeGenerationEngine { MNN, ONNX, MEDIA_PIPE, SDCPP, BONSAI, UPSCALER }
