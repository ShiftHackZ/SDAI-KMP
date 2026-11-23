package com.shifthackz.aisdv1.presentation.configuration.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.presentation.configuration.loader.ConfigurationLoaderState

/**
 * Supplies every startup configuration phase without invoking startup or provider-fetch operations.
 *
 * @author Dmitriy Moroz
 */
internal class ConfigurationLoaderPreviewParameterProvider :
    PreviewParameterProvider<ConfigurationLoaderState> {
    /**
     * Initializing, fetching, failed and launching states resolved by the real content mapper.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<ConfigurationLoaderState> =
        ConfigurationLoaderState.Status.entries
            .map { ConfigurationLoaderState(it) }.asSequence()

    /**
     * Identifies the loading phase shown above its preview.
     *
     * @param index Position in the startup phase enumeration.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String =
        ConfigurationLoaderState.Status.entries[index].name
}
