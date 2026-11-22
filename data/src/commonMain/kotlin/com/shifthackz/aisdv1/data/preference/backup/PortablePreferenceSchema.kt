package com.shifthackz.aisdv1.data.preference.backup

/**
 * Wire-to-storage allowlist. Keys outside these maps can never be imported from a backup.
 *
 * @author Dmitriy Moroz
 */
internal object PortablePreferenceSchema {
    val providers = mapOf(
        "custom" to mapOf(
            "serverUrl" to PortablePreferenceField("key_server_url", PortablePreferenceType.TEXT),
            "demoMode" to PortablePreferenceField("key_demo_mode", PortablePreferenceType.FLAG),
            "model" to PortablePreferenceField("key_sd_model", PortablePreferenceType.TEXT)
        ),
        "swarm_ui" to mapOf(
            "serverUrl" to PortablePreferenceField(
                "key_swarm_server_url",
                PortablePreferenceType.TEXT
            ),
            "model" to PortablePreferenceField("key_swarm_model", PortablePreferenceType.TEXT)
        ),
        "easy_diffusion" to mapOf(
            "serverUrl" to PortablePreferenceField(
                "key_easy_diffusion_url",
                PortablePreferenceType.TEXT
            ),
            "model" to PortablePreferenceField(
                "key_easy_diffusion_model",
                PortablePreferenceType.TEXT
            )
        ),
        "open_ai_compatible" to mapOf(
            "serverUrl" to PortablePreferenceField(
                "key_open_ai_compatible_url",
                PortablePreferenceType.TEXT
            ),
            "model" to PortablePreferenceField(
                "key_open_ai_compatible_model",
                PortablePreferenceType.TEXT
            ),
            "apiKey" to PortablePreferenceField(
                "key_open_ai_compatible_api_key",
                PortablePreferenceType.TEXT
            )
        ),
        "open_ai" to mapOf(
            "apiKey" to PortablePreferenceField("key_open_ai_api_key", PortablePreferenceType.TEXT)
        ),
        "horde" to mapOf(
            "apiKey" to PortablePreferenceField("key_horde_api_key", PortablePreferenceType.TEXT)
        ),
        "hugging_face" to mapOf(
            "apiKey" to PortablePreferenceField(
                "key_hugging_face_api_key",
                PortablePreferenceType.TEXT
            ),
            "model" to PortablePreferenceField(
                "key_hugging_face_model_key",
                PortablePreferenceType.TEXT
            )
        ),
        "stability_ai" to mapOf(
            "apiKey" to PortablePreferenceField(
                "key_stability_ai_api_key",
                PortablePreferenceType.TEXT
            ),
            "model" to PortablePreferenceField(
                "key_stability_ai_engine_id_key",
                PortablePreferenceType.TEXT
            )
        ),
        "fal_ai" to mapOf(
            "apiKey" to PortablePreferenceField("key_fal_ai_api_key", PortablePreferenceType.TEXT)
        ),
        "arli_ai" to mapOf(
            "apiKey" to PortablePreferenceField("key_arli_ai_api_key", PortablePreferenceType.TEXT),
            "model" to PortablePreferenceField("key_arli_ai_model_key", PortablePreferenceType.TEXT)
        ),
        "local" to mapOf(
            "model" to PortablePreferenceField("key_local_model_id", PortablePreferenceType.TEXT),
            "useNnapi" to PortablePreferenceField("key_local_nn_api", PortablePreferenceType.FLAG)
        ),
        "local_google_media_pipe" to mapOf(
            "model" to PortablePreferenceField(
                "key_mediapipe_model_id",
                PortablePreferenceType.TEXT
            )
        ),
        "local_stable_diffusion_cpp" to mapOf(
            "model" to PortablePreferenceField("key_sdxl_model_id", PortablePreferenceType.TEXT)
        ),
        "local_apple_core_ml" to mapOf(
            "model" to PortablePreferenceField("key_core_ml_model_id", PortablePreferenceType.TEXT)
        ),
        "local_apple_bonsai" to mapOf(
            "model" to PortablePreferenceField("key_bonsai_model_id", PortablePreferenceType.TEXT)
        ),
        "local_mnn" to mapOf(
            "model" to PortablePreferenceField("key_mnn_model_id", PortablePreferenceType.TEXT),
            "backend" to PortablePreferenceField("key_mnn_backend", PortablePreferenceType.TEXT)
        )
    )
    val settings = mapOf(
        "dynamicColors" to PortablePreferenceField(
            "key_design_dynamic_colors",
            PortablePreferenceType.FLAG
        ),
        "systemDarkTheme" to PortablePreferenceField(
            "key_design_system_dark_theme",
            PortablePreferenceType.FLAG
        ),
        "darkTheme" to PortablePreferenceField(
            "key_design_dark_theme",
            PortablePreferenceType.FLAG
        ),
        "colorToken" to PortablePreferenceField(
            "key_design_color_token_theme",
            PortablePreferenceType.TEXT
        ),
        "darkToken" to PortablePreferenceField(
            "key_design_dark_color_token_theme",
            PortablePreferenceType.TEXT
        ),
        "language" to PortablePreferenceField("key_language_code", PortablePreferenceType.TEXT),
        "galleryGrid" to PortablePreferenceField(
            "key_gallery_grid",
            PortablePreferenceType.INTEGER
        ),
        "autoSave" to PortablePreferenceField("key_ai_auto_save", PortablePreferenceType.FLAG),
        "saveToMediaStore" to PortablePreferenceField(
            "key_save_to_media_store",
            PortablePreferenceType.FLAG
        ),
        "advancedOptions" to PortablePreferenceField(
            "key_always_show_advanced_options",
            PortablePreferenceType.FLAG
        ),
        "taggedInput" to PortablePreferenceField(
            "key_prompt_tagged_input_kb",
            PortablePreferenceType.FLAG
        ),
        "connectionTimeout" to PortablePreferenceField(
            "key_connection_timeout_seconds",
            PortablePreferenceType.INTEGER
        ),
        "generationTimeout" to PortablePreferenceField(
            "key_generation_request_timeout_seconds",
            PortablePreferenceType.INTEGER
        ),
        "performanceOverlay" to PortablePreferenceField(
            "key_performance_overlay",
            PortablePreferenceType.FLAG
        ),
        "dimScreen" to PortablePreferenceField("key_dim_screen", PortablePreferenceType.FLAG),
        "backgroundGeneration" to PortablePreferenceField(
            "key_background_generation",
            PortablePreferenceType.FLAG
        ),
        "monitorConnection" to PortablePreferenceField(
            "key_monitor_connection",
            PortablePreferenceType.FLAG
        ),
        "textToImageSteps" to PortablePreferenceField(
            "key_text_to_image_sampling_steps",
            PortablePreferenceType.INTEGER
        ),
        "textToImageCfg" to PortablePreferenceField(
            "key_text_to_image_cfg_scale",
            PortablePreferenceType.TEXT
        ),
        "imageToImageSteps" to PortablePreferenceField(
            "key_image_to_image_sampling_steps",
            PortablePreferenceType.INTEGER
        ),
        "imageToImageCfg" to PortablePreferenceField(
            "key_image_to_image_cfg_scale",
            PortablePreferenceType.TEXT
        )
    )
}
