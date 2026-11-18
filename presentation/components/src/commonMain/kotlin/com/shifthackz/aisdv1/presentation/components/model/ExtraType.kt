package com.shifthackz.aisdv1.presentation.components.model

/**
 * Prompt-extra categories used to choose tag parsing and editor appearance.
 *
 * @param raw Stable type identifier used when selecting the extra catalog/editor.
 *
 * @author Dmitriy Moroz
 */
enum class ExtraType(val raw: String) {
    Lora("lora"),
    HyperNet("hyper_net");
}
