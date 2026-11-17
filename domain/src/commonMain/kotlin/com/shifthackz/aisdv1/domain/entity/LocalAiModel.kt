package com.shifthackz.aisdv1.domain.entity

/**
 * Catalog model metadata with current platform download and selection state.
 *
 * @param id Stable catalog identity or a supported synthetic custom-model identity.
 * @param type Runtime family used to parse persisted metadata and validate model layout.
 * @param name Display name shown in setup and gallery metadata.
 * @param size Human-readable download size; custom entries use NaN because their source is external.
 * @param sources Complete archive download URLs; custom entries have no remote sources.
 * @param downloaded Whether the provider's local payload is complete, or a custom selection is available.
 * @param selected Whether this identity is the independently saved model for its runtime family.
 *
 * @author Dmitriy Moroz
 */
data class LocalAiModel(
    val id: String,
    val type: Type,
    val name: String,
    val size: String,
    val sources: List<String>,
    val downloaded: Boolean = false,
    val selected: Boolean = false,
) {
    /**
     * Runtime family persisted as a stable string rather than an ordinal.
     *
     * @param key Persistent model type identifier.
     *
     * @author Dmitriy Moroz
     */
    enum class Type(val key: String) {
        Mnn("mnn"),
        ONNX("onnx"),
        MediaPipe("mediapipe"),
        Sdxl("sdxl"),
        CoreMl("coreml"),
        Bonsai("bonsai");

        companion object {
            /**
             * Resolves stored model families, preserving the historical ONNX fallback for unknown legacy values.
             *
             * @param value Stored model type key.
             *
             * @author Dmitriy Moroz
             */
            fun parse(value: String?) = entries.find { it.key == value } ?: ONNX
        }
    }

    companion object {
        /**
         * Standard custom-folder selection; MNN setup validates and snapshots its complete source bundle.
         *
         * @author Dmitriy Moroz
         */
        val CustomMnn = LocalAiModel("CUSTOM_MNN", Type.Mnn, "Custom", "NaN", emptyList())

        val CustomOnnx = LocalAiModel(
            id = "CUSTOM",
            type = Type.ONNX,
            name = "Custom",
            size = "NaN",
            sources = emptyList(),
        )

        val CustomMediaPipe = LocalAiModel(
            id = "CUSTOM_MP",
            type = Type.MediaPipe,
            name = "Custom",
            size = "NaN",
            sources = emptyList(),
        )

        val CustomSdxl = LocalAiModel(
            id = "CUSTOM_SDXL",
            type = Type.Sdxl,
            name = "Custom",
            size = "NaN",
            sources = emptyList(),
        )

        val CustomCoreMl = LocalAiModel(
            id = "CUSTOM_CORE_ML",
            type = Type.CoreMl,
            name = "Custom",
            size = "NaN",
            sources = emptyList(),
        )

        val CustomBonsai = LocalAiModel(
            id = "CUSTOM_BONSAI",
            type = Type.Bonsai,
            name = "Custom",
            size = "NaN",
            sources = emptyList(),
        )
    }
}
