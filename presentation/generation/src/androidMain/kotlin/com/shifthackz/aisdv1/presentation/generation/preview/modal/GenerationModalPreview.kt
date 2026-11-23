package com.shifthackz.aisdv1.presentation.generation.preview.modal

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewFixtures
import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewTheme
import com.shifthackz.aisdv1.presentation.generation.preview.rememberGenerationPreviewBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.shifthackz.aisdv1.presentation.generation.modal.GenerationModalRenderer
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal
import com.shifthackz.aisdv1.presentation.generation.performance.GenerationPerformanceContent
import com.shifthackz.aisdv1.presentation.generation.widget.dialog.GenerationImageBatchResultModal
import com.shifthackz.aisdv1.presentation.generation.widget.dialog.GenerationImageResultDialog

/**
 * Displays real operation dialogs with a fixed clock and successful-result widgets with fixed pixels.
 * Batch previews compose the actual sheet body directly to avoid its animated entrance.
 *
 * @param scenario Captured operation/result state and optional fixed resource panel.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationModalPreview(scenario: GenerationModalPreviewCase) {
    val image = rememberGenerationPreviewBitmap()
    GenerationPreviewTheme {
        when (val modal = scenario.modal) {
            is GenerationModal.Image.Single -> GenerationImageResultDialog(
                imageBase64 = modal.result.image,
                showSaveButton = !modal.autoSaveEnabled,
                showReportButton = modal.reportEnabled,
                imageContent = { _, modifier ->
                    Image(bitmap = image, modifier = modifier, contentDescription = null)
                },
            )

            is GenerationModal.Image.Batch -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(Modifier.fillMaxWidth()) {
                        GenerationImageBatchResultModal(
                            results = modal.results,
                            showSaveButton = !modal.autoSaveEnabled,
                            imageContent = { _, modifier ->
                                Image(
                                    bitmap = image,
                                    modifier = modifier,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            },
                        )
                    }
                }
            }

            else -> GenerationModalRenderer(
                screenModal = modal,
                elapsedText = scenario.elapsedText,
                onDismissRequest = {},
                onCancelGeneration = {},
                onSaveRequest = {},
                onReportRequest = {},
                onViewDetailRequest = {},
                onBenchmarkRequest = {},
                onSkipBenchmarkRequest = {},
                onBenchmarkContinueRequest = {},
                onBenchmarkDoNotAskRequest = {},
                performanceOverlay = {
                    if (scenario.shouldShowPerformance) GenerationPerformanceContent(samples = GenerationPreviewFixtures.samples)
                },
            )
        }
    }
}
