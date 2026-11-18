package com.shifthackz.aisdv1.presentation.components.widget.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.core.mvi.diagnostics.LocalUiDiagnosticContext
import kotlinx.coroutines.awaitCancellation

/**
 * Full-resolution editor dependency supplied by composition, separate from display sampling.
 *
 * @author Dmitriy Moroz
 */
val LocalOriginalImageLoader = staticCompositionLocalOf<OriginalImageLoader?> { null }

/**
 * Loads an explicitly opened editor off main with byte/pixel admission and lifecycle cancellation.
 *
 * @param source Original encoded content.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun rememberOriginalImage(source: String): DecodedImageBitmap {
    val isVisible = LocalMviVisibility.current
    val diagnosticContext = LocalUiDiagnosticContext.current
    val loader = LocalOriginalImageLoader.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val identity = ImageSourceIdentity(source)
    val state = remember(loader, lifecycle, identity, isVisible) {
        mutableStateOf<DecodedImageBitmap>(DecodedImageBitmap.Loading)
    }
    LaunchedEffect(state) {
        if (!isVisible) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            try {
                state.value =
                    loader?.load(source, diagnosticContext)?.let(DecodedImageBitmap::Ready)
                        ?: DecodedImageBitmap.Unavailable
                awaitCancellation()
            } finally {
                state.value = DecodedImageBitmap.Loading
            }
        }
    }
    return state.value
}
