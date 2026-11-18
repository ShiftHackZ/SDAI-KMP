package com.shifthackz.aisdv1.domain.entity.benchmark

import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Returns true when the source performs local on-device generation.
 *
 * @author Dmitriy Moroz
 */
fun ServerSource.isLocalGenerationSource(): Boolean =
    this == ServerSource.LOCAL_MICROSOFT_ONNX ||
            this == ServerSource.LOCAL_GOOGLE_MEDIA_PIPE ||
            this == ServerSource.LOCAL_STABLE_DIFFUSION_CPP ||
            this == ServerSource.LOCAL_APPLE_CORE_ML ||
            this == ServerSource.LOCAL_APPLE_BONSAI
