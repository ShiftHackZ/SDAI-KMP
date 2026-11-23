package com.shifthackz.aisdv1.presentation.settings.preview

import com.shifthackz.aisdv1.presentation.settings.platform.NoOpSettingsPlatformActions
import com.shifthackz.aisdv1.presentation.settings.platform.SettingsPlatformActions

/**
 * Exposes the requested capability flags while every platform action remains inert.
 *
 * @param isNnapiAvailable Whether ONNX acceleration rows can appear in this preview.
 * @param supportsConnectionTimeout Whether the connection-limit row can appear in this preview.
 * @author Dmitriy Moroz
 */
internal class SettingsPreviewPlatformActions(
    override val isNnapiAvailable: Boolean,
    override val supportsConnectionTimeout: Boolean,
) : SettingsPlatformActions by NoOpSettingsPlatformActions
