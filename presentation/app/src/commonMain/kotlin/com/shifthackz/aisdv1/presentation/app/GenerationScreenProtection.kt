package com.shifthackz.aisdv1.presentation.app

import androidx.compose.runtime.Composable

/**
 * Keeps the active foreground generation display awake and optionally dims it. Platform effects
 * restore the previous display settings on pause, completion, preference change or disposal.
 *
 * @param isGenerating Whether either form is generating with background generation disabled.
 * @param shouldDimScreen Whether the persisted opt-in permits minimum display brightness.
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun GenerationScreenProtection(isGenerating: Boolean, shouldDimScreen: Boolean)
