package com.shifthackz.aisdv1.core.mvi

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Visibility inherited by retained tabs; hidden content keeps state and effect delivery.
 *
 * @author Dmitriy Moroz
 */
val LocalMviVisibility = staticCompositionLocalOf { true }
