package com.shifthackz.aisdv1.presentation.components.preview.history

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.shifthackz.aisdv1.presentation.components.modal.history.InputHistoryThumbnail
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.GalleryItemMetadata
import com.shifthackz.aisdv1.presentation.components.modal.history.InputHistoryItemUi
import com.shifthackz.aisdv1.presentation.components.modal.history.InputHistoryScreenContent
import com.shifthackz.aisdv1.presentation.components.modal.history.InputHistoryState
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.preview.rememberComponentPreviewBitmap
import androidx.compose.ui.Modifier

/**
 * Shared input-history loading, empty, retry, paging and metadata/thumbnail variants.
 *
 * @author Dmitriy Moroz
 */
internal class HistoryPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("InputHistory · Loading") {
            InputHistoryScreenContent(state = InputHistoryState())
        },
        ComponentPreviewCase("InputHistory · Empty") {
            InputHistoryScreenContent(
                state = InputHistoryState(
                    loading = false,
                    canLoadMore = false
                )
            )
        },
        ComponentPreviewCase("InputHistory · Error") {
            InputHistoryScreenContent(
                state = InputHistoryState(
                    loading = false,
                    error = "Unable to load history. Try again."
                )
            )
        },
        ComponentPreviewCase("InputHistory · Text-to-image") {
            val bitmap = rememberComponentPreviewBitmap()
            InputHistoryScreenContent(
                state = InputHistoryState(
                    loading = false,
                    items = listOf(
                        historyItem(
                            1,
                            AiGenerationResult.Type.TEXT_TO_IMAGE,
                            "A quiet mountain lake"
                        )
                    ),
                    canLoadMore = false
                ),
                imageContent = { _, modifier ->
                    InputHistoryThumbnail(
                        image = bitmap,
                        modifier = modifier
                    )
                })
        },
        ComponentPreviewCase("InputHistory · Image-to-image") {
            val bitmap = rememberComponentPreviewBitmap()
            InputHistoryScreenContent(
                state = InputHistoryState(
                    loading = false,
                    items = listOf(
                        historyItem(
                            1,
                            AiGenerationResult.Type.IMAGE_TO_IMAGE,
                            "A quiet mountain lake"
                        )
                    ),
                    canLoadMore = false
                ),
                imageContent = { _, modifier ->
                    InputHistoryThumbnail(
                        image = bitmap,
                        modifier = modifier
                    )
                })
        },
        ComponentPreviewCase("InputHistory · Empty prompt") {
            InputHistoryScreenContent(
                state = InputHistoryState(
                    loading = false,
                    items = listOf(historyItem(1, AiGenerationResult.Type.TEXT_TO_IMAGE, "")),
                    canLoadMore = false
                ),
                imageContent = { _, modifier ->
                    InputHistoryThumbnail(
                        image = null,
                        modifier = modifier
                    )
                })
        },
        ComponentPreviewCase("InputHistory · Long prompt") {
            val bitmap = rememberComponentPreviewBitmap()
            InputHistoryScreenContent(
                state = InputHistoryState(
                    loading = false,
                    items = listOf(
                        historyItem(
                            1,
                            AiGenerationResult.Type.IMAGE_TO_IMAGE,
                            "A detailed mountain landscape with cinematic lighting, reflections and fine textures. A detailed mountain landscape with cinematic lighting, reflections and fine textures. A detailed mountain landscape with cinematic lighting, reflections and fine textures. A detailed mountain landscape with cinematic lighting, reflections and fine textures. "
                        )
                    ),
                    canLoadMore = false
                ),
                imageContent = { _, modifier ->
                    InputHistoryThumbnail(
                        image = bitmap,
                        modifier = modifier
                    )
                })
        },
        ComponentPreviewCase("InputHistory · Next page") {
            val bitmap = rememberComponentPreviewBitmap()
            InputHistoryScreenContent(
                state = InputHistoryState(
                    loading = false,
                    items = listOf(
                        historyItem(
                            1,
                            AiGenerationResult.Type.TEXT_TO_IMAGE,
                            "Mountain"
                        )
                    ),
                    canLoadMore = true
                ),
                imageContent = { _, modifier ->
                    InputHistoryThumbnail(
                        image = bitmap,
                        modifier = modifier
                    )
                })
        },
        ComponentPreviewCase("InputHistory · Unavailable thumbnail") {
            InputHistoryScreenContent(
                state = InputHistoryState(
                    loading = false,
                    items = listOf(
                        historyItem(
                            1,
                            AiGenerationResult.Type.IMAGE_TO_IMAGE,
                            "Mountain"
                        )
                    ),
                    canLoadMore = false
                ),
                imageContent = { _, modifier ->
                    InputHistoryThumbnail(
                        image = null,
                        modifier = modifier
                    )
                })
        },
        ComponentPreviewCase("InputHistory · Mixed pages") {
            val bitmap = rememberComponentPreviewBitmap()
            InputHistoryScreenContent(
                state = InputHistoryState(
                    loading = false,
                    items = (1L..8L).map {
                        historyItem(
                            it,
                            if (it % 2L == 0L) AiGenerationResult.Type.IMAGE_TO_IMAGE else AiGenerationResult.Type.TEXT_TO_IMAGE,
                            "Mountain $it"
                        )
                    },
                    canLoadMore = false
                ),
                imageContent = { _, modifier ->
                    InputHistoryThumbnail(
                        image = bitmap,
                        modifier = modifier
                    )
                })
        },
    ),
)

/**
 * Renders named history states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 840)
@Preview(name = "Dark", widthDp = 400, heightDp = 840, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 740, fontScale = 1.5f)
@Composable
private fun HistoryPreview(@PreviewParameter(HistoryPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}

/**
 * Fixed metadata projection for the shared input-history selector.
 *
 * @param id Stable row identity within this preview.
 * @param type Generation operation controlling denoising tags.
 * @param prompt Positive prompt, including empty/overflow cases.
 * @author Dmitriy Moroz
 */
private fun historyItem(
    id: Long,
    type: AiGenerationResult.Type,
    prompt: String
): InputHistoryItemUi =
    InputHistoryItemUi(
        GalleryItemMetadata(
            id = id, imageRevision = "sample-$id", createdAt = 0L, type = type,
            prompt = prompt, negativePrompt = "blur, noise", width = 512, height = 768,
            samplingSteps = 30, cfgScale = 7f, shouldRestoreFaces = false, sampler = "Euler",
            seed = "12345", subSeed = "", subSeedStrength = 0f, denoisingStrength = 0.65f,
            isHidden = false, isLiked = false, modelName = "Sample model",
        )
    )
