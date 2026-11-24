package com.shifthackz.aisdv1.presentation.faq.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.domain.entity.FaqCategory
import com.shifthackz.aisdv1.domain.entity.FaqContent
import com.shifthackz.aisdv1.domain.entity.FaqQuestion
import com.shifthackz.aisdv1.presentation.faq.FaqState

/**
 * One complete screen preview with a supplied load state and optional search or category selection.
 *
 * @param name Preview label in the design surface.
 * @param state FAQ load state displayed by the screen.
 * @param query Current search text for matching and empty result states.
 * @param categoryId Selected category slug, or null to show all categories.
 * @author Dmitriy Moroz
 */
internal data class FaqPreviewCase(
    val name: String,
    val state: FaqState,
    val query: String = "",
    val categoryId: String? = null,
)

/**
 * Fixed questions for rendering grouping, multi-paragraph answers, and combined search and filters.
 *
 * @author Dmitriy Moroz
 */
internal object FaqPreviewFixtures {
    val localCategoryId = "local"
    val longQuestion = FaqQuestion(
        id = "choose-provider",
        question = "How do I choose between a hosted provider and local image generation?",
        answer = listOf(
            "A hosted provider generates images on a remote server. Check its supported models, " +
                    "features, and account requirements before connecting.",
            "Local generation runs on your device. Check available storage and start with a small " +
                    "model and a lower image resolution.",
        ),
        tags = listOf("provider", "models"),
    )
    val categories = listOf(
        FaqCategory(
            id = "basics",
            label = "Basics",
            items = listOf(
                FaqQuestion(
                    id = "what-is-sdai",
                    question = "What is SDAI?",
                    answer = listOf("SDAI connects to image generation providers and on-device engines."),
                    tags = listOf("overview"),
                ),
                longQuestion,
            ),
        ),
        FaqCategory(
            id = "setup",
            label = "Setup",
            items = listOf(
                FaqQuestion(
                    id = "connect-server",
                    question = "How do I connect to my own server?",
                    answer = listOf("Select your provider in Configuration and enter the server URL."),
                    tags = listOf("connection", "server"),
                ),
            ),
        ),
        FaqCategory(
            id = localCategoryId,
            label = "Local Generation",
            items = listOf(
                FaqQuestion(
                    id = "slow-generation",
                    question = "Why is local generation slow on my phone?",
                    answer = listOf("Model size, quantization, and resolution all affect how quickly an image generates."),
                    tags = listOf("performance", "nnapi"),
                ),
                FaqQuestion(
                    id = "model-storage",
                    question = "How much space do downloaded models need?",
                    answer = listOf("Check the catalog for the model size and check Storage usage before downloading."),
                    tags = listOf("storage"),
                ),
            ),
        ),
    )
    val online =
        FaqPreviewCase("Online answers", FaqState.Ready(FaqContent(categories, isCached = false)))
    val cached =
        FaqPreviewCase("Saved answers", FaqState.Ready(FaqContent(categories, isCached = true)))
    val category = online.copy(name = "Category applied", categoryId = localCategoryId)
}

/**
 * Lists the screen states in the same order for the light and dark design surfaces.
 *
 * @author Dmitriy Moroz
 */
internal class FaqPreviewParameterProvider : PreviewParameterProvider<FaqPreviewCase> {
    private val cases = listOf(
        FaqPreviewCase("Loading", FaqState.Loading),
        FaqPreviewCase("Load error", FaqState.Error),
        FaqPreviewFixtures.online,
        FaqPreviewFixtures.cached,
        FaqPreviewFixtures.category,
        FaqPreviewFixtures.category.copy(
            name = "Search within category",
            query = "quantization nnapi"
        ),
        FaqPreviewFixtures.online.copy(name = "No matching questions", query = "unmatched search"),
    )

    override val values: Sequence<FaqPreviewCase> = cases.asSequence()

    override fun getDisplayName(index: Int): String = cases[index].name
}
