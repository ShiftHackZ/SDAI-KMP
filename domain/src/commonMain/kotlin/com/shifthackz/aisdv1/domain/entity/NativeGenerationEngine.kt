package com.shifthackz.aisdv1.domain.entity

/**
 * Android engines whose inference must execute outside the application host process.
 *
 * @author Dmitriy Moroz
 */
enum class NativeGenerationEngine { ONNX, MEDIA_PIPE, SDCPP, BONSAI }
