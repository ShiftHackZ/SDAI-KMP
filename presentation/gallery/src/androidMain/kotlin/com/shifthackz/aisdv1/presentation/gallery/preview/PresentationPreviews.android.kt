package com.shifthackz.aisdv1.presentation.gallery.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailContent
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailScreenContent
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailState
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailTab
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState

@Preview(name = "Gallery details info", widthDp = 360, heightDp = 740, showBackground = true)
@Composable
private fun GalleryDetailInfoPreview() {
    PreviewTheme {
        GalleryDetailScreenContent(
            state = GalleryDetailState(
                loading = false,
                tabs = listOf(GalleryDetailTab.IMAGE, GalleryDetailTab.INFO),
                selectedTab = GalleryDetailTab.INFO,
                content = GalleryDetailContent(
                    showReportButton = true,
                    generationType = AiGenerationResult.Type.TEXT_TO_IMAGE,
                    id = 1L,
                    imageBase64 = "",
                    image = null,
                    inputImageBase64 = null,
                    inputImage = null,
                    createdAt = "2026-06-10 16:30",
                    type = "txt2img",
                    prompt = "cinematic mobile app preview",
                    negativePrompt = "low quality",
                    size = "512 X 512",
                    samplingSteps = "20",
                    cfgScale = "7.0",
                    restoreFaces = "No",
                    sampler = "DPM++ 2M",
                    seed = "123456",
                    subSeed = "",
                    subSeedStrength = "0.0",
                    denoisingStrength = "0.75",
                    hidden = false,
                ),
            ),
        )
    }
}

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
