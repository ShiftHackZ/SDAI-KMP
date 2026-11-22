package com.shifthackz.aisdv1.presentation.components.model

import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Describes the selected integration with the same localized provider summaries used during setup.
 * Host-specific MNN and Bonsai details follow the device platform.
 *
 * @param platform Host platform selecting the applicable local-engine description.
 * @return Provider-specific summary without account data, addresses or credentials.
 * @author Dmitriy Moroz
 */
fun ServerSource.description(platform: Platform): String = Localization.string(
    when (this) {
        ServerSource.AUTOMATIC1111 -> "hint_server_setup_sub_title"
        ServerSource.SWARM_UI -> "hint_swarm_ui_sub_title"
        ServerSource.EASY_DIFFUSION -> "easy_diffusion_subtitle"
        ServerSource.HORDE -> "hint_server_horde_sub_title"
        ServerSource.HUGGING_FACE -> "hint_hugging_face_sub_title"
        ServerSource.OPEN_AI_COMPATIBLE -> "open_ai_compatible_help"
        ServerSource.OPEN_AI_OAUTH -> "open_ai_oauth_help"
        ServerSource.OPEN_AI -> "hint_open_ai_sub_title"
        ServerSource.STABILITY_AI -> "hint_stability_ai_sub_title"
        ServerSource.FAL_AI -> "hint_fal_ai_sub_title"
        ServerSource.ARLI_AI -> "hint_arli_ai_sub_title"
        ServerSource.LOCAL_MICROSOFT_ONNX -> "hint_local_diffusion_sub_title"
        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> "hint_mediapipe_sub_title"
        ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> "hint_sdxl_sub_title"
        ServerSource.LOCAL_APPLE_CORE_ML -> "hint_core_ml_sub_title"
        ServerSource.LOCAL_MNN -> if (platform == Platform.IOS) "hint_mnn_profile_ios" else "hint_mnn_profile"
        ServerSource.LOCAL_APPLE_BONSAI -> if (platform == Platform.IOS) "hint_bonsai_sub_title" else "hint_bonsai_android_sub_title"
    },
)
