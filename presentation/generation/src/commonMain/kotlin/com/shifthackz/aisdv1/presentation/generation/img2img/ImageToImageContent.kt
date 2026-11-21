@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.generation.img2img

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.generation.modal.GenerationModalRenderer
import com.shifthackz.aisdv1.presentation.generation.modal.embedding.EmbeddingScreen
import com.shifthackz.aisdv1.presentation.generation.modal.extras.ExtrasScreen
import com.shifthackz.aisdv1.presentation.generation.modal.tag.EditTagDialog
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.GenerationBottomToolbar
import com.shifthackz.aisdv1.presentation.components.widget.work.BackgroundWorkWidget

/**
 * Localized labels and provider capability explanations for image-to-image rendering.
 *
 * @param title Image-to-image toolbar heading.
 * @param pickGallery Action label for native photo selection.
 * @param pickCamera Action label for native camera capture.
 * @param pickRandom Action label for selecting a saved random gallery image.
 * @param clear Action label for removing the selected source.
 * @param configureProvider Action label for changing provider configuration.
 * @param generate Generation submission label.
 * @param generating Label shown during an active generation.
 * @param inputImage Heading of the source-image card.
 * @param inPaint Action label for opening mask editing.
 * @param sourceUnavailable Explanation when the selected source cannot generate from images.
 * @param unsupportedTitle Heading for the unsupported-provider state.
 * @param compatibleUnsupportedSubtitle Explanation of compatible-provider image-to-image limitations.
 * @param openAiUnsupportedSubtitle First explanation of official OpenAI limitations.
 * @param openAiUnsupportedSubtitle2 Additional official OpenAI capability explanation.
 * @param localUnsupportedSubtitle First explanation of local-provider limitations.
 * @param localUnsupportedSubtitle2 Additional local-provider capability explanation.
 * @author Dmitriy Moroz
 */
data class ImageToImageStrings(
    val title: String = Localization.string("title_image_to_image"),
    val pickGallery: String = Localization.string("action_image_picker_gallery"),
    val pickCamera: String = Localization.string("action_image_picker_camera"),
    val pickRandom: String = Localization.string("action_image_picker_random"),
    val clear: String = Localization.string("action_clear"),
    val configureProvider: String = Localization.string("action_change_configuration"),
    val generate: String = Localization.string("action_generate"),
    val generating: String = Localization.string("notification_running_title"),
    val inputImage: String = Localization.string("title_input_image"),
    val inPaint: String = Localization.string("in_paint_title"),
    val sourceUnavailable: String = Localization.string("error_source_img2img_unsupported"),
    val unsupportedTitle: String = Localization.string("local_no_img2img_support_title"),
    val compatibleUnsupportedSubtitle: String = Localization.string("open_ai_compatible_img2img_unsupported"),
    val openAiUnsupportedSubtitle: String = Localization.string("openai_no_img2img_support_sub_title"),
    val openAiUnsupportedSubtitle2: String = Localization.string("openai_no_img2img_support_sub_title_2"),
    val localUnsupportedSubtitle: String = Localization.string("local_no_img2img_support_sub_title"),
    val localUnsupportedSubtitle2: String = Localization.string("local_no_img2img_support_sub_title_2"),
)

/**
 * Renders the retained image-to-image form, source acquisition and generation action with local editing sheets.
 *
 * @param state Current provider/form/source state owned by the ViewModel.
 * @param processIntent Publishes field, generation, export and navigation requests.
 * @param modifier Layout modifiers for the form scaffold.
 * @param strings Localized labels and provider capability explanations.
 * @param performanceOverlay Optional live resource panel supplied by the application root.
 * @param useDrawerNavigation Whether the toolbar opens the home drawer instead of navigating back.
 * @author Dmitriy Moroz
 */
@Composable
fun ImageToImageContent(
    state: ImageToImageState,
    processIntent: (ImageToImageIntent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ImageToImageStrings = ImageToImageStrings(),
    useDrawerNavigation: Boolean = false,
    performanceOverlay: @Composable () -> Unit = {},
) {
    val promptChipTextFieldState = remember { mutableStateOf(TextFieldValue()) }
    val negativePromptChipTextFieldState = remember { mutableStateOf(TextFieldValue()) }
    var activeModal by remember { mutableStateOf<ImageToImagePanel?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                processIntent(
                                    if (useDrawerNavigation) {
                                        ImageToImageIntent.OpenDrawer
                                    } else {
                                        ImageToImageIntent.NavigateBack
                                    },
                                )
                            },
                        ) {
                            Icon(
                                imageVector = if (useDrawerNavigation) {
                                    Icons.Default.Menu
                                } else {
                                    Icons.AutoMirrored.Filled.ArrowBack
                                },
                                contentDescription = null,
                            )
                        }
                    },
                    title = {
                        Text(
                            text = strings.title,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { activeModal = ImageToImagePanel.History },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                            )
                        }
                    },
                )
                BackgroundWorkWidget(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(vertical = 4.dp),
                )
            }
        },
        bottomBar = {
            val configureOnly = !state.sourceSupportsImageToImage
            GenerationBottomToolbar(
                strokeAccentState = configureOnly || state.canGenerate,
                mode = state.mode,
                prompt = state.prompt,
                negativePrompt = state.negativePrompt,
                onExtraSelected = { prompt, negativePrompt, type ->
                    activeModal = ImageToImagePanel.Extras(prompt, negativePrompt, type)
                },
                onEmbeddingsSelected = { prompt, negativePrompt ->
                    activeModal = ImageToImagePanel.Embeddings(prompt, negativePrompt)
                },
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    enabled = configureOnly || state.canGenerate,
                    onClick = {
                        if (configureOnly) {
                            processIntent(ImageToImageIntent.ConfigureProvider)
                        } else {
                            flushPendingTaggedText(
                                state = state,
                                promptChipTextFieldState = promptChipTextFieldState,
                                negativePromptChipTextFieldState = negativePromptChipTextFieldState,
                                processIntent = processIntent,
                            )
                            processIntent(ImageToImageIntent.Generate)
                        }
                    },
                ) {
                    if (state.generating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else if (!configureOnly) {
                        Icon(
                            imageVector = Icons.Default.AutoFixNormal,
                            contentDescription = null,
                        )
                    }
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = when {
                            configureOnly -> strings.configureProvider
                            state.generating -> strings.generating
                            else -> strings.generate
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            if (state.loadingConfiguration) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp),
                )
            } else if (!state.sourceSupportsImageToImage) {
                UnsupportedImageToImageBody(
                    modifier = Modifier.fillMaxSize(),
                    mode = state.mode,
                    strings = strings,
                )
            } else {
                ImageToImageBody(
                    state = state,
                    strings = strings,
                    promptChipTextFieldState = promptChipTextFieldState,
                    negativePromptChipTextFieldState = negativePromptChipTextFieldState,
                    processIntent = processIntent,
                    onInPaintClick = { processIntent(ImageToImageIntent.NavigateToInPaint) },
                )
            }
        }
    }
    state.editTag?.let { editTag ->
        EditTagDialog(
            prompt = editTag.prompt,
            negativePrompt = editTag.negativePrompt,
            tag = editTag.tag,
            isNegative = editTag.isNegative,
            onDismissRequest = { processIntent(ImageToImageIntent.DismissEditTag) },
            onNewPrompts = { prompt, negativePrompt ->
                processIntent(ImageToImageIntent.ApplyPrompts(prompt, negativePrompt))
            },
        )
    }
    when (val modal = activeModal) {
        null -> Unit
        is ImageToImagePanel.Embeddings -> EmbeddingScreen(
            prompt = modal.prompt,
            negativePrompt = modal.negativePrompt,
            onNewPrompts = { prompt, negativePrompt ->
                processIntent(ImageToImageIntent.ApplyPrompts(prompt, negativePrompt))
            },
            onClose = { activeModal = null },
        )

        is ImageToImagePanel.Extras -> ExtrasScreen(
            prompt = modal.prompt,
            negativePrompt = modal.negativePrompt,
            type = modal.type,
            onNewPrompts = { prompt, negativePrompt ->
                processIntent(ImageToImageIntent.ApplyPrompts(prompt, negativePrompt))
            },
            onClose = { activeModal = null },
        )

        ImageToImagePanel.History -> GenerationHistoryDialog(
            onClose = { activeModal = null },
            onGenerationSelected = { ai ->
                processIntent(ImageToImageIntent.ApplyGenerationResult(ai, inputImage = true))
                activeModal = null
            },
        )
    }
    GenerationModalRenderer(
        generationStartedAt = state.generationStartedAt,
        performanceOverlay = performanceOverlay,
        screenModal = state.screenModal,
        onDismissRequest = { processIntent(ImageToImageIntent.DismissModal) },
        onCancelGeneration = { processIntent(ImageToImageIntent.CancelGeneration) },
        onSaveRequest = { processIntent(ImageToImageIntent.SaveGenerationResults(it)) },
        onReportRequest = { processIntent(ImageToImageIntent.ReportGenerationResult(it)) },
        onViewDetailRequest = { processIntent(ImageToImageIntent.ViewGenerationResult(it)) },
        onBenchmarkRequest = { processIntent(ImageToImageIntent.RunBenchmarkFromPrompt) },
        onSkipBenchmarkRequest = { processIntent(ImageToImageIntent.SkipBenchmarkPrompt) },
        onBenchmarkContinueRequest = { processIntent(ImageToImageIntent.ContinueAfterBenchmarkWarning) },
        onBenchmarkDoNotAskRequest = { processIntent(ImageToImageIntent.SuppressBenchmarkWarningAndContinue) },
    )
}
