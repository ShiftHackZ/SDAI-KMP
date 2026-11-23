package com.shifthackz.aisdv1.presentation.donate.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState
import com.shifthackz.aisdv1.presentation.donate.DonateBrandIcon
import com.shifthackz.aisdv1.presentation.donate.DonateScreenContent
import com.shifthackz.aisdv1.presentation.donate.donateScreenStrings

/**
 * Displays the real donation page with fixed supporter data and inert navigation and support intents.
 *
 * @param scenario Loading state, fictional acknowledgements and initial list position.
 * @author Dmitriy Moroz
 */
@Composable
internal fun DonatePreview(scenario: DonatePreviewCase) {
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = isSystemInDarkTheme(),
            shouldUseSystemColorPalette = false,
        ),
        applySystemBars = false,
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            DonateScreenContent(
                state = scenario.state,
                strings = donateScreenStrings(),
                processIntent = {},
                modifier = Modifier.fillMaxSize(),
                listState = rememberLazyListState(initialFirstVisibleItemIndex = scenario.firstVisibleItem),
                brandIcon = { DonateBrandIcon() },
            )
        }
    }
}
