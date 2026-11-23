package com.shifthackz.aisdv1.presentation.onboarding.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.shifthackz.aisdv1.core.extensions.gesturesDisabled
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.presentation.onboarding.buildOnBoardingText
import com.shifthackz.aisdv1.presentation.onboarding.onBoardingDensity
import com.shifthackz.aisdv1.presentation.onboarding.onBoardingPhoneAspectRatio
import com.shifthackz.aisdv1.presentation.onboarding.onBoardingPhoneWidthFraction
import com.shifthackz.aisdv1.presentation.settings.SettingsScreenContent
import com.shifthackz.aisdv1.presentation.settings.SettingsState
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState
import com.shifthackz.aisdv1.presentation.components.widget.frame.PhoneFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Illustrates appearance preferences inside a noninteractive phone with a cycling accent and grid.
 * The demonstration is isolated from saved preferences and freezes its animation in design previews.
 *
 * @param modifier Layout bounds for the onboarding page.
 * @param darkThemeToken Dark palette demonstrated inside the phone.
 * @param appVersion Version label displayed in the illustrative Settings footer.
 * @param isPageVisible Whether the pager currently exposes this page and its animation may run.
 * @author Dmitriy Moroz
 */
@Composable
internal fun LookAndFeelPageContent(
    modifier: Modifier = Modifier,
    darkThemeToken: DarkThemeToken,
    appVersion: String,
    isPageVisible: Boolean = false,
) = Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    val scope = rememberCoroutineScope()
    val isDarkTheme = true
    val shouldAnimate = isPageVisible && !LocalInspectionMode.current
    var themeState by remember {
        mutableStateOf(
            AiSdAppThemeState(
                shouldUseSystemColorPalette = false,
                shouldUseSystemDarkTheme = false,
                isDarkTheme = isDarkTheme,
                darkThemeToken = darkThemeToken,
            ),
        )
    }
    var settingsState by remember {
        mutableStateOf(
            SettingsState(
                loading = false,
                onBoardingDemo = true,
                colorToken = themeState.colorToken,
                darkThemeToken = themeState.darkThemeToken,
                darkTheme = isDarkTheme,
                appVersion = appVersion,
            ),
        )
    }
    Spacer(modifier = Modifier.weight(1f))
    Text(
        text = buildOnBoardingText(Localization.string("on_boarding_page_ui_title")),
        fontSize = 24.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight(450),
    )
    Spacer(modifier = Modifier.weight(1f))
    PhoneFrame(
        modifier = Modifier.fillMaxWidth(onBoardingPhoneWidthFraction),
    ) {
        CompositionLocalProvider(LocalDensity provides onBoardingDensity) {
            AiSdAppTheme(
                state = themeState,
                applySystemBars = false,
            ) {
                SettingsScreenContent(
                    modifier = Modifier.aspectRatio(onBoardingPhoneAspectRatio),
                    state = settingsState,
                    backgroundWorkContent = {},
                )
                Box(
                    modifier = Modifier
                        .aspectRatio(onBoardingPhoneAspectRatio)
                        .gesturesDisabled(),
                )
            }
        }
    }
    DisposableEffect(shouldAnimate) {
        val job = scope.launch {
            while (shouldAnimate) {
                delay(700)
                val colorToken = ColorToken.entries.random()
                settingsState = settingsState.copy(
                    galleryGrid = Grid.entries.random(),
                    colorToken = colorToken,
                )
                themeState = themeState.copy(
                    colorToken = colorToken,
                )
            }
        }
        onDispose { job.cancel() }
    }
    Spacer(modifier = Modifier.weight(1f))
}
