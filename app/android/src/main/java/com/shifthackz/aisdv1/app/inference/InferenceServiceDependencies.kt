package com.shifthackz.aisdv1.app.inference

/**
 * Framework-service composition supplied by the application root without host services.
 *
 * @param engines Child-only runtime factory.
 * @param files Private request/result exchange.
 * @author Dmitriy Moroz
 */
internal class InferenceServiceDependencies(
    val engines: NativeInferenceEngineFactory,
    val files: GenerationFileStore
)
