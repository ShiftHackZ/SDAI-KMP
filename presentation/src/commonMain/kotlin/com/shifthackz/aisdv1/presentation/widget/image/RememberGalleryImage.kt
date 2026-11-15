package com.shifthackz.aisdv1.presentation.widget.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.core.mvi.diagnostics.LocalUiDiagnosticContext
import kotlinx.coroutines.awaitCancellation

/**
 * Cancels obsolete/hidden/stopped display requests and resets state for each image identity.
 *
 * @param loader Injected display cache.
 * @param id Persisted gallery identity.
 * @param revision Content revision.
 * @param isInput Original-input selection.
 * @param edge Measured display pixels; zero postpones decoding until layout.
 * @param transientBase64 Unsaved original; null selects persisted revision-checked reads.
 * @author Dmitriy Moroz
 */
@Composable
internal fun rememberGalleryImage(
    loader: GalleryImageLoader,
    id: Long,
    revision: String,
    isInput: Boolean = false,
    edge: Int = 0,
    transientBase64: String? = null
): State<DecodedImageBitmap> {
    val diagnosticContext = LocalUiDiagnosticContext.current
    val bucket = if (edge <= 0) 0 else ((edge.coerceIn(64, 2048) + 63) / 64) * 64
    val isVisible = LocalMviVisibility.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val state = remember(loader, id, revision, isInput, bucket, isVisible, lifecycle) {
        mutableStateOf<DecodedImageBitmap>(if (isVisible && bucket > 0) DecodedImageBitmap.Loading else DecodedImageBitmap.Unavailable)
    }
    LaunchedEffect(state, isVisible, lifecycle) {
        if (!isVisible || bucket == 0) {
            state.value = DecodedImageBitmap.Unavailable; return@LaunchedEffect
        }
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            try {
                state.value = DecodedImageBitmap.Loading
                val image = if (transientBase64 == null) loader.load(
                    id,
                    revision,
                    isInput,
                    bucket,
                    diagnosticContext
                )
                else loader.loadTransient(
                    revision,
                    transientBase64,
                    isInput,
                    bucket,
                    diagnosticContext
                )
                state.value =
                    image?.let(DecodedImageBitmap::Ready) ?: DecodedImageBitmap.Unavailable
                awaitCancellation()
            } finally {
                state.value = DecodedImageBitmap.Unavailable
            }
        }
    }
    return state
}
