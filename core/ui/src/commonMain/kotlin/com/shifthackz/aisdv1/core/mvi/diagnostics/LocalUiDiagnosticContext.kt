package com.shifthackz.aisdv1.core.mvi.diagnostics

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Optional data context supplied by MVI composition; unsupported platforms and release builds keep the null default.
 *
 * @author Dmitriy Moroz
 */
val LocalUiDiagnosticContext = staticCompositionLocalOf<UiDiagnosticContext?> { null }
