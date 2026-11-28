package com.shifthackz.aisdv1.presentation.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoubleArrow
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.shifthackz.aisdv1.presentation.components.widget.action.OutlinedButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.presentation.onboarding.page.UniversalPageContent
import com.shifthackz.aisdv1.presentation.onboarding.page.FormPageContent
import com.shifthackz.aisdv1.presentation.onboarding.page.ProviderPageContent
import com.shifthackz.aisdv1.presentation.onboarding.page.LocalDiffusionPageContent
import com.shifthackz.aisdv1.presentation.onboarding.page.LookAndFeelPageContent
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentBottomBarWindowInsets
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Hosts onboarding and its module-owned generation, provider and appearance illustrations.
 *
 * @param launchSource Controls whether the first page can return to settings.
 * @param state Current immutable render state supplied by the owning screen.
 * @param processIntent Sends user actions to the owning screen state holder.
 * @param modifier Layout modifiers for the pager and navigation controls.
 * @param pagerState Current page and scroll position; defaults to the first visible page.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun OnBoardingContent(
    launchSource: LaunchSource,
    state: OnBoardingState,
    processIntent: (OnBoardingIntent) -> Unit,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState(
        pageCount = { visibleOnBoardingPages(state.showLocalDiffusionPage).size },
    ),
) {
    val pages = remember(state.showLocalDiffusionPage) {
        visibleOnBoardingPages(state.showLocalDiffusionPage)
    }
    val scope = rememberCoroutineScope()

    var scrollAnimationJob: Job? by remember { mutableStateOf(null) }

    fun scrollToPage(page: Int) {
        if (scrollAnimationJob != null) return
        if (pagerState.isScrollInProgress) return
        scrollAnimationJob = scope.launch {
            pagerState.animateScrollToPage(
                page = page.coerceIn(0, pages.lastIndex),
                animationSpec = onBoardingPageAnimation,
            )
        }.apply { invokeOnCompletion { scrollAnimationJob = null } }
    }

    OnBoardingBackHandler(
        enabled = pagerState.currentPage > 0 || pagerState.isScrollInProgress,
    ) {
        scrollToPage(pagerState.currentPage - 1)
    }

    PlatformScaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            CompositionLocalProvider(LocalPlatformControlBackgroundColor provides MaterialTheme.colorScheme.surface) {
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(persistentBottomBarWindowInsets())
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(onBoardingPhoneWidthFraction)
                            .padding(bottom = 32.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val backAlpha by animateFloatAsState(
                            targetValue = if (pagerState.currentPage > 0 || launchSource == LaunchSource.SETTINGS) {
                                1f
                            } else {
                                0f
                            },
                            label = "back_button_animation",
                        )
                        OutlinedButton(
                            modifier = Modifier
                                .alpha(backAlpha)
                                .size(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp),
                            onClick = {
                                if (pagerState.currentPage > 0) {
                                    scrollToPage(pagerState.currentPage - 1)
                                } else if (launchSource == LaunchSource.SETTINGS && !pagerState.isScrollInProgress) {
                                    processIntent(OnBoardingIntent.Navigate)
                                }
                            },
                        ) {
                            Icon(
                                modifier = Modifier.rotate(180f),
                                imageVector = Icons.Default.DoubleArrow,
                                contentDescription = Localization.string("action_back"),
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(pages.size) { index ->
                                val color = if (index == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .drawBehind { drawCircle(color = color) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        PrimaryButton(
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp),
                            onClick = {
                                if (pagerState.currentPage == pages.lastIndex && !pagerState.isScrollInProgress) {
                                    processIntent(OnBoardingIntent.Navigate)
                                } else {
                                    scrollToPage(pagerState.currentPage + 1)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = if (pagerState.currentPage == pages.lastIndex) {
                                    Icons.Default.Check
                                } else {
                                    Icons.Default.DoubleArrow
                                },
                                contentDescription = Localization.string("next"),
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
                beyondViewportPageCount = pages.size,
                userScrollEnabled = true,
            ) { index ->
                when (pages[index]) {
                    OnBoardingPage.Universal -> UniversalPageContent()

                    OnBoardingPage.Providers -> ProviderPageContent(
                        isPageVisible = pagerState.currentPage == index,
                    )

                    OnBoardingPage.Form -> FormPageContent()

                    OnBoardingPage.LocalDiffusion -> LocalDiffusionPageContent()

                    OnBoardingPage.LookAndFeel -> LookAndFeelPageContent(
                        darkThemeToken = state.darkThemeToken,
                        appVersion = state.appVersion,
                        isPageVisible = pagerState.currentPage == index,
                    )
                }
            }
        }
    }
}
