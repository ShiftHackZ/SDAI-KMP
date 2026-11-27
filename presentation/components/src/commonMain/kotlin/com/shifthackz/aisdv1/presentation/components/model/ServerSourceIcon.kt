package com.shifthackz.aisdv1.presentation.components.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.presentation.components.widget.icon.BrandIcons

/**
 * Chooses the provider icon, branding local providers with the current platform.
 *
 * @param platform Platform whose provider availability and readiness apply.
 *
 * @author Dmitriy Moroz
 */
fun ServerSource.providerIcon(platform: Platform): ImageVector = when (this) {
    ServerSource.AUTOMATIC1111,
    ServerSource.SWARM_UI,
    ServerSource.EASY_DIFFUSION,
    ServerSource.OPEN_AI_COMPATIBLE,
        -> Icons.Default.Computer

    ServerSource.HORDE,
    ServerSource.HUGGING_FACE,
    ServerSource.OPEN_AI,
    ServerSource.OPEN_AI_OAUTH,
    ServerSource.STABILITY_AI,
    ServerSource.FAL_AI,
    ServerSource.ARLI_AI,
        -> Icons.Default.Cloud

    ServerSource.LOCAL_MNN,
    ServerSource.LOCAL_MICROSOFT_ONNX,
    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
    ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
    ServerSource.LOCAL_APPLE_CORE_ML,
    ServerSource.LOCAL_APPLE_BONSAI -> localProviderIcon(platform)
}

/**
 * Uses the current platform's local-provider artwork for independent upscaler sources.
 *
 * @param platform Host platform selecting Android or Apple branding.
 * @author Dmitriy Moroz
 */
fun UpscalerSource.providerIcon(platform: Platform): ImageVector = when (this) {
    UpscalerSource.LOCAL_ESRGAN, UpscalerSource.LOCAL_MNN -> localProviderIcon(platform)
}

private fun localProviderIcon(platform: Platform): ImageVector = when (platform) {
    Platform.ANDROID -> Icons.Default.Android
    Platform.IOS -> BrandIcons.Apple
}
