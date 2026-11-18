package com.shifthackz.aisdv1.presentation.configuration.setup.content

import com.shifthackz.aisdv1.core.localization.Localization

/**
 * Localized copy bundle for provider setup.
 *
 * Keeping these values in one immutable object lets previews and tests override text without
 * touching the global `Localization` singleton. Function values wrap formatted resources.
 *
 * @param title Title of the provider setup wizard.
 * @param next Label for advancing to the next setup step.
 * @param connect Label for applying the selected provider configuration.
 * @param setup Label for opening provider configuration.
 * @param backContentDescription Accessible description of the back navigation control.
 * @param loadingConfiguration Status text displayed while saved configuration is loading.
 * @param communicatingTitle Heading shown while setup checks a provider connection.
 * @param communicatingSubtitle Explanation shown while provider communication is in progress.
 * @param errorTitle Heading of the setup error dialog.
 * @param ok Label for acknowledging a setup message.
 * @param cancel Label for dismissing an action without applying it.
 * @param clear Label for clearing a setup filter or text value.
 * @param connectLocalHostTitle Heading of the warning about connecting to the local device.
 * @param connectLocalHostText Explanation of the local-device connection warning.
 * @param sourceTitle Heading of the provider-selection step.
 * @param configureTitle Heading of the provider-configuration step.
 * @param sourceSearchHint Hint shown in the provider search field.
 * @param sourceFilterAll Label for showing every provider type.
 * @param sourceFilterSelfHosted Label for the self-hosted provider filter.
 * @param sourceFilterCloud Label for the cloud provider filter.
 * @param sourceFilterLocal Label for the on-device provider filter.
 * @param sourceFilterReadiness Heading of the provider readiness filters.
 * @param sourceFilterTags Heading of the provider capability filters.
 * @param sourceNoResults Empty-state message when no providers match the active filters.
 * @param sourceOptionsTitle Heading of provider discovery options.
 * @param sourceFilterAction Accessible label for opening provider filters.
 * @param sourceSortAction Accessible label for opening provider sorting options.
 * @param sourceSortTitle Heading of the provider sort dialog.
 * @param sourceSortDefault Label for the default provider order.
 * @param sourceSortRecent Label for sorting recently updated providers first.
 * @param sourceSortOldest Label for sorting oldest provider updates first.
 * @param readinessExperimental Badge text for experimental providers.
 * @param readinessAlpha Badge text for alpha providers.
 * @param readinessBeta Badge text for beta providers.
 * @param readinessStable Badge text for stable providers.
 * @param sourceVersion Formats the provider version displayed in discovery rows.
 * @param serverUrl Label of the server URL field.
 * @param apiKey Label of the provider API key field.
 * @param model Label of the remote model selector.
 * @param authTitle Heading of the HTTP authentication controls.
 * @param anonymous Label for anonymous HTTP access.
 * @param httpBasic Label for HTTP Basic authentication.
 * @param login Label of the HTTP Basic username field.
 * @param password Label of the HTTP Basic password field.
 * @param showPassword Accessible label for revealing the password.
 * @param hidePassword Accessible label for concealing the password.
 * @param instructions Label for opening provider setup instructions.
 * @param demoMode Label of the demonstration-server toggle.
 * @param useDefaultHordeKey Label for using the anonymous default AI Horde key.
 * @param hordeAbout Explanation of the AI Horde provider.
 * @param hordeGetKey Label for obtaining an AI Horde API key.
 * @param huggingFaceAbout Explanation of the Hugging Face provider.
 * @param openAiAbout Explanation of the OpenAI provider.
 * @param falAiAbout Explanation of the Fal AI provider.
 * @param arliAiAbout Explanation of the Arli AI provider.
 * @param stabilityAbout Explanation of the Stability AI provider.
 * @param automaticTitle Provider name for a self-hosted AUTOMATIC1111 server.
 * @param automaticFormTitle Heading of the AUTOMATIC1111 configuration form.
 * @param automaticSubtitle Summary of the AUTOMATIC1111 integration.
 * @param automaticHint Instructions for enabling the AUTOMATIC1111 server API.
 * @param demoHint Explanation of the shared demonstration server.
 * @param swarmTitle Provider name for SwarmUI.
 * @param swarmFormTitle Heading of the SwarmUI configuration form.
 * @param swarmSubtitle Summary of the SwarmUI integration.
 * @param swarmHint Instructions for enabling remote access to SwarmUI.
 * @param hordeUsage Usage guidance for AI Horde generation.
 * @param hordeTitle Heading of the AI Horde configuration form.
 * @param hordeSubtitle Summary of the AI Horde integration.
 * @param huggingFaceTitle Heading of the Hugging Face configuration form.
 * @param huggingFaceSubtitle Summary of Hugging Face image generation.
 * @param openAiCompatibleTitle Heading of the OpenAI-compatible server form.
 * @param openAiCompatibleSubtitle Explanation of compatible-server configuration.
 * @param openAiCompatibleUrl Label of the compatible-server base URL field.
 * @param optionalApiKey Label indicating that the compatible-server API key may be omitted.
 * @param connectionUnverifiedTitle Heading shown when a compatible-server connection could not be verified.
 * @param connectionUnverifiedText Explanation of connecting without successful endpoint verification.
 * @param openAiTitle Heading of the OpenAI configuration form.
 * @param openAiSubtitle Summary of the OpenAI integration.
 * @param falAiTitle Heading of the Fal AI configuration form.
 * @param falAiSubtitle Summary of the Fal AI integration.
 * @param arliAiTitle Heading of the Arli AI configuration form.
 * @param arliAiSubtitle Summary of the Arli AI integration.
 * @param stabilityTitle Heading of the Stability AI configuration form.
 * @param stabilitySubtitle Summary of the Stability AI integration.
 * @param localDiffusionTitle Heading of the local ONNX configuration form.
 * @param localDiffusionSubtitle Summary of local ONNX diffusion.
 * @param mediaPipeTitle Heading of the MediaPipe configuration form.
 * @param mediaPipeSubtitle Summary of MediaPipe diffusion.
 * @param sdxlTitle Heading of the stable-diffusion.cpp configuration form.
 * @param sdxlSubtitle Summary of local stable-diffusion.cpp generation.
 * @param coreMlTitle Provider name for Core ML generation.
 * @param coreMlSubtitle Summary of Core ML generation on supported Apple devices.
 * @param mnnTitle Provider name for MNN generation.
 * @param mnnSubtitle Summary of the supported MNN model profile.
 * @param bonsaiTitle Provider name for Bonsai generation on Apple platforms.
 * @param bonsaiAndroidTitle Provider name for Bonsai generation on Android.
 * @param bonsaiSubtitle Summary of Bonsai generation on Apple platforms.
 * @param bonsaiAndroidSubtitle Summary of Bonsai generation on Android.
 * @param localWarning Resource and compatibility warning shown for local generation.
 * @param localCustomSwitch Label of the custom local model toggle.
 * @param localPermissionHeader Heading shown when custom-model storage permission is missing.
 * @param localPermissionTitle Explanation of the custom-model storage permission requirement.
 * @param localPermissionButton Label for opening the storage permission settings.
 * @param localPathHeader Heading of the custom model path section.
 * @param localPathTitle Label of the custom model folder field.
 * @param localPathButton Label for opening the existing custom model folder picker.
 * @param localCustomTitle Title of a custom model entry in the local model list.
 * @param localCustomSubtitle Explanation of the custom model entry.
 * @param download Label for starting a catalog model download.
 * @param delete Label for removing an owned catalog model.
 * @param retry Label for retrying a failed download or catalog request.
 * @param downloadFailed Message shown when a model archive download fails.
 * @param reset Label for restoring the default custom model path.
 * @param deleteLocalModelTitle Heading of the owned-model deletion confirmation.
 *
 * @author Dmitriy Moroz
 */
data class ServerSetupStrings(
    val title: String = Localization.string("title_server_setup"),
    val next: String = Localization.string("next"),
    val connect: String = Localization.string("action_connect"),
    val setup: String = Localization.string("action_setup"),
    val backContentDescription: String = Localization.string("action_back"),
    val loadingConfiguration: String = Localization.string("splash_status_fetching"),
    val communicatingTitle: String = Localization.string("communicating_progress_title"),
    val communicatingSubtitle: String = Localization.string("communicating_progress_sub_title"),
    val errorTitle: String = Localization.string("error_title"),
    val ok: String = Localization.string("ok"),
    val cancel: String = Localization.string("cancel"),
    val clear: String = Localization.string("action_clear"),
    val connectLocalHostTitle: String = Localization.string("setup_localhost_url_title"),
    val connectLocalHostText: String = Localization.string("setup_localhost_url_text"),
    val sourceTitle: String = Localization.string("srv_step_1"),
    val configureTitle: String = Localization.string("srv_step_2"),
    val sourceSearchHint: String = Localization.string("provider_search_hint"),
    val sourceFilterAll: String = Localization.string("provider_filter_type_all"),
    val sourceFilterSelfHosted: String = Localization.string("provider_filter_type_self_hosted"),
    val sourceFilterCloud: String = Localization.string("provider_filter_type_cloud"),
    val sourceFilterLocal: String = Localization.string("provider_filter_type_local"),
    val sourceFilterReadiness: String = Localization.string("provider_filter_readiness"),
    val sourceFilterTags: String = Localization.string("provider_filter_tags"),
    val sourceNoResults: String = Localization.string("provider_no_results"),
    val sourceOptionsTitle: String = Localization.string("provider_options_title"),
    val sourceFilterAction: String = Localization.string("provider_action_filters"),
    val sourceSortAction: String = Localization.string("provider_action_sort"),
    val sourceSortTitle: String = Localization.string("provider_sort_title"),
    val sourceSortDefault: String = Localization.string("provider_sort_default"),
    val sourceSortRecent: String = Localization.string("provider_sort_recent"),
    val sourceSortOldest: String = Localization.string("provider_sort_oldest"),
    val readinessExperimental: String = Localization.string("provider_readiness_experimental"),
    val readinessAlpha: String = Localization.string("provider_readiness_alpha"),
    val readinessBeta: String = Localization.string("provider_readiness_beta"),
    val readinessStable: String = Localization.string("provider_readiness_stable"),
    val sourceVersion: (String) -> String = { version ->
        Localization.string(
            "provider_version",
            version
        )
    },
    val serverUrl: String = Localization.string("hint_server_url"),
    val apiKey: String = Localization.string("hint_server_horde_api_key"),
    val model: String = Localization.string("hint_hugging_face_model"),
    val authTitle: String = Localization.string("auth_title"),
    val anonymous: String = Localization.string("auth_anonymous"),
    val httpBasic: String = Localization.string("auth_http_basic"),
    val login: String = Localization.string("hint_login"),
    val password: String = Localization.string("hint_password"),
    val showPassword: String = Localization.string("action_show_password"),
    val hidePassword: String = Localization.string("action_hide_password"),
    val instructions: String = Localization.string("settings_item_instructions"),
    val demoMode: String = Localization.string("settings_item_demo"),
    val useDefaultHordeKey: String = Localization.string("hint_server_horde_use_default_api_key"),
    val hordeAbout: String = Localization.string("hint_server_horde_about"),
    val hordeGetKey: String = Localization.string("hint_server_horde_get_api_key"),
    val huggingFaceAbout: String = Localization.string("hint_hugging_face_about"),
    val openAiAbout: String = Localization.string("hint_open_ai_about"),
    val falAiAbout: String = Localization.string("hint_fal_ai_about"),
    val arliAiAbout: String = Localization.string("hint_arli_ai_about"),
    val stabilityAbout: String = Localization.string("hint_stability_ai_about"),
    val automaticTitle: String = Localization.string("srv_type_own"),
    val automaticFormTitle: String = Localization.string("hint_server_setup_title"),
    val automaticSubtitle: String = Localization.string("hint_server_setup_sub_title"),
    val automaticHint: String = Localization.string("hint_args_warning"),
    val demoHint: String = Localization.string("hint_demo_mode"),
    val swarmTitle: String = Localization.string("srv_type_swarm_ui"),
    val swarmFormTitle: String = Localization.string("hint_swarm_ui_title"),
    val swarmSubtitle: String = Localization.string("hint_swarm_ui_sub_title"),
    val swarmHint: String = Localization.string("hint_args_swarm_ui_warning"),
    val hordeUsage: String = Localization.string("hint_server_horde_usage"),
    val hordeTitle: String = Localization.string("hint_server_horde_title"),
    val hordeSubtitle: String = Localization.string("hint_server_horde_sub_title"),
    val huggingFaceTitle: String = Localization.string("hint_hugging_face_title"),
    val huggingFaceSubtitle: String = Localization.string("hint_hugging_face_sub_title"),
    val openAiCompatibleTitle: String = Localization.string("srv_type_open_ai_compatible"),
    val openAiCompatibleSubtitle: String = Localization.string("open_ai_compatible_help"),
    val openAiCompatibleUrl: String = Localization.string("open_ai_compatible_url"),
    val optionalApiKey: String = Localization.string("open_ai_compatible_optional_key"),
    val connectionUnverifiedTitle: String = Localization.string("open_ai_compatible_unverified_title"),
    val connectionUnverifiedText: String = Localization.string("open_ai_compatible_unverified_text"),
    val openAiTitle: String = Localization.string("hint_open_ai_title"),
    val openAiSubtitle: String = Localization.string("hint_open_ai_sub_title"),
    val falAiTitle: String = Localization.string("hint_fal_ai_title"),
    val falAiSubtitle: String = Localization.string("hint_fal_ai_sub_title"),
    val arliAiTitle: String = Localization.string("hint_arli_ai_title"),
    val arliAiSubtitle: String = Localization.string("hint_arli_ai_sub_title"),
    val stabilityTitle: String = Localization.string("hint_stability_ai_title"),
    val stabilitySubtitle: String = Localization.string("hint_stability_ai_sub_title"),
    val localDiffusionTitle: String = Localization.string("hint_local_diffusion_title"),
    val localDiffusionSubtitle: String = Localization.string("hint_local_diffusion_sub_title"),
    val mediaPipeTitle: String = Localization.string("hint_mediapipe_title"),
    val mediaPipeSubtitle: String = Localization.string("hint_mediapipe_sub_title"),
    val sdxlTitle: String = Localization.string("hint_sdxl_title"),
    val sdxlSubtitle: String = Localization.string("hint_sdxl_sub_title"),
    val coreMlTitle: String = "Silicon Diffusion Core ML",
    val coreMlSubtitle: String = Localization.string("hint_core_ml_sub_title"),
    val mnnTitle: String = Localization.string("srv_type_mnn"),
    val mnnSubtitle: String = Localization.string("hint_mnn_profile"),
    val bonsaiTitle: String = Localization.string("hint_bonsai_title"),
    val bonsaiAndroidTitle: String = Localization.string("hint_bonsai_android_title"),
    val bonsaiSubtitle: String = Localization.string("hint_bonsai_sub_title"),
    val bonsaiAndroidSubtitle: String = Localization.string("hint_bonsai_android_sub_title"),
    val localWarning: String = Localization.string("hint_local_diffusion_warning"),
    val localCustomSwitch: String = Localization.string("model_local_custom_switch"),
    val localPermissionHeader: String = Localization.string("model_local_permission_header"),
    val localPermissionTitle: String = Localization.string("model_local_permission_title"),
    val localPermissionButton: String = Localization.string("model_local_permission_button"),
    val localPathHeader: String = Localization.string("model_local_path_header"),
    val localPathTitle: String = Localization.string("model_local_path_title"),
    val localPathButton: String = Localization.string("model_local_path_button"),
    val localCustomTitle: String = Localization.string("model_local_custom_title"),
    val localCustomSubtitle: String = Localization.string("model_local_custom_sub_title"),
    val download: String = Localization.string("download"),
    val delete: String = Localization.string("delete"),
    val retry: String = Localization.string("retry"),
    val downloadFailed: String = Localization.string("error_download_fail"),
    val reset: String = Localization.string("action_reset"),
    val deleteLocalModelTitle: String = Localization.string("interaction_delete_local_model_title"),
)
