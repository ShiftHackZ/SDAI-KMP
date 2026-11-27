package com.shifthackz.aisdv1.presentation.generation.preview.upscaler

import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerFormState
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerState

/**
 * A standalone screen snapshot and the scroll position of its first visible card.
 *
 * @param name Label shown in the Android Studio preview picker.
 * @param state Captured image, scale and modal state.
 * @param formState Fixed model catalog and setup availability.
 * @param firstVisibleItemIndex Provider, input or parameters card to show first.
 * @param shouldShowImage Whether the preview supplies decoded source pixels.
 * @author Dmitriy Moroz
 */
internal data class UpscalerScreenPreviewCase(
    val name: String,
    val state: UpscalerState = UpscalerPreviewFixtures.screen,
    val formState: UpscalerFormState = UpscalerPreviewFixtures.form,
    val firstVisibleItemIndex: Int = 0,
    val shouldShowImage: Boolean = true,
)
