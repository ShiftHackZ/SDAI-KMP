package com.shifthackz.aisdv1.presentation.components.core

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Transfers generation metadata from gallery/history to the retained generation forms.
 * Each form keeps its latest pending payload until consumed or cleared. Application DI shares
 * one instance across publishers and forms.
 *
 * @author Dmitriy Moroz
 */
class GenerationFormUpdateEvent {

    private val sTxt2Img = MutableStateFlow<Payload>(Payload.None)

    private val sImg2Img = MutableStateFlow<Payload>(Payload.None)

    /**
     * Replaces the pending payload for the selected form.
     *
     * @param generation Source generation metadata and images to restore.
     * @param route Target generation form, independent of the source result's type.
     * @param inputImage Whether image-to-image restoration uses the source input instead of output bytes.
     *
     * @author Dmitriy Moroz
     */
    fun update(
        generation: AiGenerationResult,
        route: AiGenerationResult.Type,
        inputImage: Boolean,
    ) {
        when (route) {
            AiGenerationResult.Type.TEXT_TO_IMAGE -> sTxt2Img.value = Payload.T2IForm(generation)
            AiGenerationResult.Type.IMAGE_TO_IMAGE -> sImg2Img.value =
                Payload.I2IForm(generation, inputImage)
        }
    }

    /**
     * Clears pending restoration payloads for both forms.
     *
     * @author Dmitriy Moroz
     */
    fun clear() {
        sTxt2Img.value = Payload.None
        sImg2Img.value = Payload.None
    }

    /**
     * Clears the pending text-to-image restoration payload.
     *
     * @author Dmitriy Moroz
     */
    fun clearTxt2Img() {
        sTxt2Img.value = Payload.None
    }

    /**
     * Clears the pending image-to-image restoration payload.
     *
     * @author Dmitriy Moroz
     */
    fun clearImg2Img() {
        sImg2Img.value = Payload.None
    }

    /**
     * Returns and clears the pending text-to-image payload, or null if none is waiting.
     *
     * @author Dmitriy Moroz
     */
    fun consumeTxt2ImgForm(): Payload.T2IForm? =
        (sTxt2Img.value as? Payload.T2IForm)?.also { clearTxt2Img() }

    /**
     * Returns and clears the pending image-to-image payload, or null if none is waiting.
     *
     * @author Dmitriy Moroz
     */
    fun consumeImg2ImgForm(): Payload.I2IForm? =
        (sImg2Img.value as? Payload.I2IForm)?.also { clearImg2Img() }

    /**
     * Observes the latest text-to-image restoration payload, including None after consumption/clear.
     *
     * @author Dmitriy Moroz
     */
    fun observeTxt2ImgForm(): Flow<Payload> = sTxt2Img.asStateFlow()

    /**
     * Observes the latest image-to-image restoration payload, including None after consumption/clear.
     *
     * @author Dmitriy Moroz
     */
    fun observeImg2ImgForm(): Flow<Payload> = sImg2Img.asStateFlow()

    /**
     * Latest pending restoration input for one retained generation form.
     *
     * @author Dmitriy Moroz
     */
    sealed interface Payload {
        /**
         * No restoration payload is waiting for this form.
         *
         * @author Dmitriy Moroz
         */
        data object None : Payload

        /**
         * Restores text-generation parameters from a saved result.
         *
         * @param ai Source metadata whose prompt and generation settings should be restored.
         *
         * @author Dmitriy Moroz
         */
        data class T2IForm(val ai: AiGenerationResult) : Payload

        /**
         * Restores image-generation parameters and one of the source images.
         *
         * @param ai Source metadata and image bytes.
         * @param inputImage Whether to use the source input image instead of its generated output.
         *
         * @author Dmitriy Moroz
         */
        data class I2IForm(val ai: AiGenerationResult, val inputImage: Boolean) : Payload
    }
}
