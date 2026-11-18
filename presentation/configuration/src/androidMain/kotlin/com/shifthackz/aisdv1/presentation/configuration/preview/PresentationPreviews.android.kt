package com.shifthackz.aisdv1.presentation.configuration.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupContent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState

@Preview(name = "Configuration", widthDp = 360, heightDp = 740, showBackground = true)
@Composable
private fun ServerSetupContentPreview() {
    PreviewTheme {
        ServerSetupContent(
            state = ServerSetupState(
                showBackNavArrow = true,
                step = ServerSetupState.Step.CONFIGURE,
                loadingConfiguration = false,
                serverUrl = "https://a1111.svc.home.arpa",
                demoMode = true,
            ),
            processIntent = {},
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
