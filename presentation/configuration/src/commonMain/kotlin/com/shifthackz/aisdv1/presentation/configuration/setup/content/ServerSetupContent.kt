@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.configuration.setup.content

import com.shifthackz.aisdv1.presentation.configuration.setup.source.SourceSelectionToolbar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryActionButton
import com.shifthackz.aisdv1.presentation.components.widget.action.ScreenActionBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import com.shifthackz.aisdv1.presentation.configuration.setup.component.LocalModelDownloadAction
import com.shifthackz.aisdv1.presentation.configuration.setup.localCatalogType
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.configuration.setup.component.ConfigurationStepBar
import com.shifthackz.aisdv1.presentation.configuration.setup.component.OpenAiOAuthActionBar
import com.shifthackz.aisdv1.presentation.configuration.setup.component.ServerSetupLoading
import com.shifthackz.aisdv1.presentation.configuration.setup.component.ServerSetupModal
import com.shifthackz.aisdv1.presentation.configuration.setup.component.mainButtonEnabled
import com.shifthackz.aisdv1.presentation.configuration.setup.form.local.ConfigurationStep
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.source.SourceFiltersBottomSheet
import com.shifthackz.aisdv1.presentation.configuration.setup.source.SourceSelectionEffects
import com.shifthackz.aisdv1.presentation.configuration.setup.source.SourceSelectionStep
import com.shifthackz.aisdv1.presentation.configuration.setup.source.SourceSortBottomSheet
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets

/**
 * Hosts source selection and a scrolling configuration form with actions pinned above system insets.
 *
 * @param state Current wizard, provider and form state.
 * @param processIntent Dispatches navigation, configuration and account actions through MVI.
 * @param modifier Bounds supplied by the navigation host.
 * @param strings Localized shared wizard labels.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ServerSetupContent(
    state: ServerSetupState,
    processIntent: (ServerSetupIntent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ServerSetupStrings = ServerSetupStrings(),
) {
    val sourceListState = rememberLazyListState()
    val configurationListState = rememberLazyListState()

    SourceSelectionEffects(state, strings, sourceListState, processIntent)

    LaunchedEffect(state.mode) {
        configurationListState.scrollToItem(0)
    }

    Box(modifier = modifier.fillMaxSize()) {
        PlatformScaffold(
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
                    )
                    ConfigurationStepBar(
                        currentStep = state.step,
                        strings = strings,
                    )
                    if (state.step == ServerSetupState.Step.SOURCE) SourceSelectionToolbar(
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
        ) { paddingValues ->
            if (state.loadingConfiguration) {
                ServerSetupLoading(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    text = strings.loadingConfiguration,
                )
            } else {
                when (state.step) {
                    ServerSetupState.Step.SOURCE -> SourceSelectionStep(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        state = state,
                        strings = strings,
                        listState = sourceListState,
                        processIntent = processIntent,
                    )

                    ServerSetupState.Step.CONFIGURE -> ConfigurationStep(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        state = state,
                        strings = strings,
                        listState = configurationListState,
                        processIntent = processIntent,
                    )
                }
            }
        }
        ServerSetupModal(
            modal = state.modal,
            strings = strings,
            processIntent = processIntent,
        )
        if (state.modal == ServerSetupState.Modal.SourceFilters) {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { value -> value != SheetValue.PartiallyExpanded },
            )
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { processIntent(ServerSetupIntent.DismissDialog) },
                shape = RectangleShape,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                SourceFiltersBottomSheet(
                    state = state,
                    strings = strings,
                    processIntent = processIntent,
                )
            }
        }
        if (state.modal == ServerSetupState.Modal.SourceSort) {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { value -> value != SheetValue.PartiallyExpanded },
            )
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { processIntent(ServerSetupIntent.DismissDialog) },
                shape = RectangleShape,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                SourceSortBottomSheet(
                    state = state,
                    strings = strings,
                    processIntent = processIntent,
                )
            }
        }
    }
}
