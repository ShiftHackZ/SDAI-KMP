@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.configuration.setup.content

import com.shifthackz.aisdv1.presentation.configuration.setup.source.SourceSelectionToolbarReveal
import com.shifthackz.aisdv1.presentation.configuration.setup.source.SourceSelectionToolbarHeight
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.isPlatformToolbarNative
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryActionButton
import com.shifthackz.aisdv1.presentation.components.widget.action.ScreenActionBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.configuration.setup.component.LocalModelDownloadAction
import com.shifthackz.aisdv1.presentation.configuration.setup.localCatalogType
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.configuration.setup.component.ConfigurationStepBar
import com.shifthackz.aisdv1.presentation.configuration.setup.component.configurationStepBarHeight
import com.shifthackz.aisdv1.presentation.configuration.setup.component.OpenAiOAuthActionBar
import com.shifthackz.aisdv1.presentation.configuration.setup.component.mainButtonEnabled
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets


/**
 * Keeps wizard navigation, step indicators and actions fixed around the moving step body.
 *
 * @param state Step snapshot for this retained wizard page.
 * @param processIntent Existing MVI event sink for fixed navigation and actions.
 * @param strings Localized wizard labels.
 * @param modifier Bounds of the fixed scaffold and moving content.
 * @param content Step pager consuming scaffold insets and the platform toolbar underlap.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ServerSetupScaffold(
    state: ServerSetupState,
    processIntent: (ServerSetupIntent) -> Unit,
    strings: ServerSetupStrings,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val searchFraction by animateFloatAsState(
        targetValue = if (state.step == ServerSetupState.Step.SOURCE) 1f else 0f,
        animationSpec = tween(240),
        label = "provider-search-reveal",
    )
    PlatformScaffold(
        modifier = modifier,
        canUnderlapToolbar = true,
        topBar = {
            Column {
                PlatformTopAppBar(
                    title = {
                        Text(
                            text = strings.title,
                            style = platformToolbarTitleStyle(MaterialTheme.typography.headlineMedium),
                        )
                    },
                    navigationIcon = {
                        if (state.showBackNavArrow || state.step != ServerSetupState.Step.SOURCE) {
                            ToolbarBackButton(
                                onClick = { processIntent(ServerSetupIntent.NavigateBack) },
                                icon = Icons.AutoMirrored.Outlined.ArrowBack
                            )
                        }
                    },
                    windowInsets = persistentTopAppBarWindowInsets(),
                    bottomContentHeight = configurationStepBarHeight() +
                            if (isPlatformToolbarNative) SourceSelectionToolbarHeight * searchFraction else 0.dp,
                    bottomContent = {
                        Column {
                            ConfigurationStepBar(currentStep = state.step, strings = strings)
                            if (isPlatformToolbarNative) {
                                SourceSelectionToolbarReveal(
                                    searchFraction,
                                    state,
                                    strings,
                                    processIntent
                                )
                            }
                        }
                    },
                )
                if (!isPlatformToolbarNative) SourceSelectionToolbarReveal(
                    searchFraction,
                    state,
                    strings,
                    processIntent
                )
            }
        },
        bottomBar = {
            if (state.step == ServerSetupState.Step.CONFIGURE && state.mode == ServerSource.OPEN_AI_OAUTH) {
                OpenAiOAuthActionBar(
                    state = state,
                    strings = strings,
                    processIntent = processIntent
                )
            } else {
                ScreenActionBar(verticalArrangement = Arrangement.Top) {
                    if (state.step == ServerSetupState.Step.CONFIGURE && state.mode.localCatalogType != null) {
                        LocalModelDownloadAction(state, processIntent)
                    }
                    PrimaryActionButton(
                        isEnabled = !state.loadingConfiguration && state.mainButtonEnabled,
                        onClick = { processIntent(ServerSetupIntent.MainButtonClick) },
                    ) {
                        Text(
                            text = when (state.step) {
                                ServerSetupState.Step.SOURCE -> strings.next
                                ServerSetupState.Step.CONFIGURE -> when (state.mode) {
                                    ServerSource.LOCAL_MICROSOFT_ONNX,
                                    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
                                    ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> strings.setup

                                    else -> strings.connect
                                }
                            },
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        },
        content = content,
    )
}
