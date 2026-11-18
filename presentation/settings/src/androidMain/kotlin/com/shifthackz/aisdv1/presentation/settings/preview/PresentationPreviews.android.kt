package com.shifthackz.aisdv1.presentation.settings.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.settings.content.ContentSettingsState
import com.shifthackz.aisdv1.presentation.settings.SettingsState
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState

@Preview(name = "Settings content", widthDp = 360, heightDp = 740, showBackground = true)
@Composable
private fun SettingsContentPreview() {
    PreviewTheme {
        ContentSettingsState(
            modifier = Modifier.fillMaxSize(),
            state = SettingsState(
                loading = false,
                serverSource = ServerSource.AUTOMATIC1111,
                sdModels = listOf("sd15-pruned-emaonly.safetensors"),
                sdModelSelected = "sd15-pruned-emaonly.safetensors",
                backgroundGenerationAvailable = true,
                monitorConnectivity = true,
                autoSaveAiResults = true,
                saveToMediaStore = true,
                appVersion = "1.0.0 (201)",
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
