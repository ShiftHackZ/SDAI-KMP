@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.settings

import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarIconButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.MviComponent
import org.koin.mp.KoinPlatform.getKoin
import com.shifthackz.aisdv1.core.navigation.router.SettingsRouter
import com.shifthackz.aisdv1.presentation.settings.content.ContentSettingsState
import com.shifthackz.aisdv1.presentation.settings.modal.SettingsModalRenderer
import com.shifthackz.aisdv1.presentation.settings.platform.NoOpSettingsPlatformActions
import com.shifthackz.aisdv1.presentation.settings.platform.SettingsPlatformActions
import com.shifthackz.aisdv1.presentation.settings.platform.rememberSettingsPlatformActions
import com.shifthackz.aisdv1.presentation.components.widget.work.BackgroundWorkWidget
import org.koin.core.parameter.parametersOf

/**
 * Main Settings tab screen shown inside the app bottom navigation scaffold.
 *
 * @param modifier Compose modifier applied to the Settings route.
 * @param router Optional router override for tests and previews; Koin supplies the app router otherwise.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    router: SettingsRouter? = null,
) {
    val koin = remember { getKoin() }
    val platformActions = rememberSettingsPlatformActions()
    val resolvedRouter = remember(koin, router) {
        router ?: koin.get<SettingsRouter>()
    }
    val viewModel = remember(
        koin,
        resolvedRouter,
        platformActions,
    ) {
        koin.get<SettingsViewModel> {
            parametersOf(resolvedRouter, platformActions)
        }
    }

    MviComponent(viewModel = viewModel) { state, intentHandler ->
        SettingsScreenContent(
            modifier = modifier,
            state = state,
            platformActions = platformActions,
            processIntent = intentHandler,
        )
    }
}

/**
 * Stateless Settings content used by the screen and tests.
 *
 * @param modifier Compose modifier applied to the Settings content root.
 * @param state Current Settings MVI state.
 * @param platformActions Platform permission and settings bridge used by Settings actions.
 * @param processIntent Intent sink used to route UI actions back to the ViewModel.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    state: SettingsState,
    platformActions: SettingsPlatformActions = NoOpSettingsPlatformActions,
    processIntent: (SettingsIntent) -> Unit = {},
) {
    Box(modifier) {
        PlatformScaffold(
            canUnderlapToolbar = true,
            topBar = {
                Column {
                    PlatformTopAppBar(
                        navigationIcon = {
                            ToolbarIconButton(onClick = {
                                processIntent(SettingsIntent.OpenDrawer)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = Localization.string("action_menu"),
                                )
                            }
                        },
                        title = {
                            Text(
                                text = Localization.string("title_settings"),
                                style = platformToolbarTitleStyle(MaterialTheme.typography.headlineMedium),
                            )
                        },
                        windowInsets = WindowInsets(0, 0, 0, 0),
                    )
                    BackgroundWorkWidget(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(vertical = 4.dp),
                    )
                }
            },
            content = { paddingValues ->
                val contentModifier = Modifier
                    .padding(
                        horizontal = paddingValues.calculateStartPadding(
                            LocalLayoutDirection.current,
                        ),
                    )
                    .padding(top = paddingValues.calculateTopPadding())

                ContentSettingsState(
                    modifier = contentModifier,
                    state = state,
                    platformActions = platformActions,
                    processIntent = processIntent,
                )
            },
        )
        SettingsModalRenderer(
            screenModal = state.screenModal,
            platformActions = platformActions,
            processIntent = processIntent,
        )
    }
}
