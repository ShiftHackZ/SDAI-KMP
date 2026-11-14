package com.shifthackz.aisdv1.presentation.widget.input

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shifthackz.aisdv1.core.validation.seed.SeedValidator
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.screen.img2img.ImageToImageIntentProcessor
import com.shifthackz.aisdv1.presentation.screen.img2img.ImageToImageState
import com.shifthackz.aisdv1.presentation.screen.img2img.mapToPayload
import com.shifthackz.aisdv1.presentation.screen.img2img.toImageToImageIntent
import com.shifthackz.aisdv1.presentation.screen.txt2img.TextToImageIntentProcessor
import com.shifthackz.aisdv1.presentation.screen.txt2img.TextToImageState
import com.shifthackz.aisdv1.presentation.screen.txt2img.mapToPayload
import com.shifthackz.aisdv1.presentation.screen.txt2img.toTextToImageIntent
import io.mockk.mockk

/**
 * Connects the shared form to production event mapping, intent processing and payload mapping.
 * Unrelated actions are strict mocks so a seed test cannot silently invoke generation or navigation.
 *
 * @param isImg2Img whether to exercise the image-to-image screen's state and intent path.
 * @param seedValidator real seed validation contract resolved by the test composition root.
 * @param nativeSeedValidator real seed validator configured for native providers by the test composition root.
 * @author Dmitriy Moroz
 */
internal class GenerationSeedFormFixture(
    private val isImg2Img: Boolean,
    private val seedValidator: SeedValidator,
    private val nativeSeedValidator: SeedValidator,
) {
    private var textState by mutableStateOf(
        TextToImageState(
            mode = ServerSource.AUTOMATIC1111,
            advancedOptionsVisible = true,
            seed = "123",
            subSeed = "456",
        ),
    )
    private var imageState by mutableStateOf(
        ImageToImageState(
            mode = ServerSource.AUTOMATIC1111,
            advancedOptionsVisible = true,
            seed = "123",
            subSeed = "456",
        ),
    )
    private val textProcessor = TextToImageIntentProcessor(
        router = mockk(),
        updateState = { textState = it(textState).withSeedValidation(seedValidator, nativeSeedValidator) },
        generate = mockk(),
        cancelGeneration = mockk(),
        saveImage = mockk(),
        shareImage = mockk(),
        saveGenerationResults = mockk(),
        viewGenerationResult = mockk(),
        reportGenerationResult = mockk(),
        applyGenerationResult = mockk(),
    )
    private val imageProcessor = ImageToImageIntentProcessor(
        router = mockk(),
        updateState = { imageState = it(imageState).withSeedValidation(seedValidator, nativeSeedValidator) },
        pickImage = mockk(),
        pickRandomImage = mockk(),
        generate = mockk(),
        cancelGeneration = mockk(),
        saveImage = mockk(),
        shareImage = mockk(),
        saveGenerationResults = mockk(),
        viewGenerationResult = mockk(),
        reportGenerationResult = mockk(),
        applyGenerationResult = mockk(),
    )

    val state: GenerationInputFormState
        get() = if (isImg2Img) imageState else textState

    val hasValidationErrors: Boolean
        get() = if (isImg2Img) imageState.hasValidationErrors else textState.hasValidationErrors

    /**
     * Returns the seed pair carried by the selected screen's actual generation payload.
     *
     * @author Dmitriy Moroz
     */
    fun payloadSeeds(): Pair<String, String> = if (isImg2Img) {
        imageState.mapToPayload().let { it.seed to it.subSeed }
    } else {
        textState.mapToPayload().let { it.seed to it.subSeed }
    }

    /**
     * Routes a form event through the same mapping and processor as its generation screen.
     *
     * @param event action emitted by the rendered production control.
     * @author Dmitriy Moroz
     */
    fun process(event: GenerationInputFormEvent) {
        if (isImg2Img) {
            imageProcessor.process(checkNotNull(event.toImageToImageIntent()))
        } else {
            textProcessor.process(checkNotNull(event.toTextToImageIntent()))
        }
    }
}
