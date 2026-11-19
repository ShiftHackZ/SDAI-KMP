package com.shifthackz.aisdv1.presentation.app

import androidx.compose.runtime.Composable

/**
 * Applies native display overrides while generation and the host are active. Restores the
 * previous display settings before replacing an override, on pause, completion or disposal.
 *
 * @param isGenerating Whether foreground generation needs the display kept awake.
 * @param shouldDimScreen Whether the current interaction state permits minimum brightness.
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun GenerationScreenEffect(isGenerating: Boolean, shouldDimScreen: Boolean)
