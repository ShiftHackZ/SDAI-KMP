package com.shifthackz.aisdv1.presentation.generation.preview

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.withoutVisualEffect
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiSampler
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformLazySurface
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageContent
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageState
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageContent
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageState
import com.shifthackz.aisdv1.presentation.generation.widget.input.generationInputFormItems

@Preview(name = "Txt2Img screen", widthDp = 360, heightDp = 740, showBackground = true)
@Composable
private fun TextToImageContentPreview() {
    PreviewTheme {
        TextToImageContent(
            state = previewTextToImageState(),
            processIntent = {},
            useDrawerNavigation = true,
        )
    }
}

@Preview(name = "Img2Img unsupported", widthDp = 360, heightDp = 740, showBackground = true)
@Composable
private fun ImageToImageUnsupportedPreview() {
    PreviewTheme {
        ImageToImageContent(
            state = previewImageToImageState().copy(mode = ServerSource.OPEN_AI),
            processIntent = {},
            useDrawerNavigation = true,
        )
    }
}

@Preview(name = "Generation form", widthDp = 360, heightDp = 560, showBackground = true)
@Composable
private fun GenerationInputFormPreview() {
    PreviewTheme {
        val promptState = remember { mutableStateOf(TextFieldValue()) }
        val negativePromptState = remember { mutableStateOf(TextFieldValue()) }
        val listState = rememberLazyListState()
        val overscrollEffect = rememberOverscrollEffect()
        PlatformLazySurface(
            state = listState,
            firstItemIndex = 0,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            overscrollEffect = overscrollEffect,
        ) {
            LazyColumn(
                state = listState,
                overscrollEffect = overscrollEffect?.withoutVisualEffect()
            ) {
                generationInputFormItems(
                    state = previewTextToImageState(),
                    isImg2Img = false,
                    isExpanded = true,
                    onToggleExpanded = {},
                    promptChipTextFieldState = promptState,
                    negativePromptChipTextFieldState = negativePromptState,
                    onEvent = {},
                )
            }
        }
    }
}

private fun previewTextToImageState() = TextToImageState(
    loadingConfiguration = false,
    onBoardingDemo = true,
    advancedOptionsVisible = true,
    prompt = "portrait photo, soft light, detailed",
    negativePrompt = "low quality, blurry",
    availableSamplers = listOf("DPM++ 2M", "Euler a", "LCM"),
    selectedSampler = "DPM++ 2M",
    batchCount = 2,
)

private fun previewImageToImageState() = ImageToImageState(
    loadingConfiguration = false,
    onBoardingDemo = true,
    prompt = "restore details",
    negativePrompt = "artifacts",
    availableSamplers = StabilityAiSampler.entries.map { "$it" },
    selectedSampler = "${StabilityAiSampler.entries.first()}",
)

@Composable
private fun PreviewTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = darkTheme,
            colorToken = ColorToken.MAUVE,
        ),
        applySystemBars = false,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            content = content,
        )
    }
}
