@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.configuration.setup.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalBackTransitionState
import com.shifthackz.aisdv1.presentation.components.widget.navigation.PlatformBackHandler
import com.shifthackz.aisdv1.presentation.components.widget.navigation.rememberBackTransitionState
import com.shifthackz.aisdv1.presentation.components.widget.navigation.ProgrammaticPager
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalNavigationInputEnabled
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.isPlatformToolbarNative
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.component.ServerSetupModal
import com.shifthackz.aisdv1.presentation.configuration.setup.component.ServerSetupLoading
import com.shifthackz.aisdv1.presentation.configuration.setup.form.local.ConfigurationStep
import com.shifthackz.aisdv1.presentation.configuration.setup.source.SourceSelectionStep
import com.shifthackz.aisdv1.presentation.configuration.setup.source.SourceFiltersBottomSheet
import com.shifthackz.aisdv1.presentation.configuration.setup.source.SourceSelectionEffects
import com.shifthackz.aisdv1.presentation.configuration.setup.source.SourceSortBottomSheet

/**
 * Owns wizard scroll positions, discovery effects and a side-effect-free local Back preview.
 * iOS Configuration-to-provider gestures commit the existing intent only after visual completion.
 * Android Back changes the internal pager, independently of the application's route animation.
 *
 * @param state Current wizard, provider and form state.
 * @param processIntent Dispatches navigation, configuration and account actions through MVI.
 * @param modifier Bounds supplied by the navigation host.
 * @param strings Localized shared wizard labels.
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
    val localBack = rememberBackTransitionState()
    val routeBack = LocalBackTransitionState.current
    val currentState by rememberUpdatedState(state)
    val currentIntent by rememberUpdatedState(processIntent)
    val isConfiguration = state.step == ServerSetupState.Step.CONFIGURE
    var hasOpenedConfiguration by remember { mutableStateOf(isConfiguration) }
    val shouldAnimateStepBack = isConfiguration && isPlatformToolbarNative
    val canGoBack = state.showBackNavArrow || isConfiguration
    val canStartGesture =
        canGoBack && state.modal == ServerSetupState.Modal.None && !state.loadingConfiguration
    PlatformBackHandler(
        isEnabled = canGoBack,
        canStartGesture = canStartGesture && (!isConfiguration || shouldAnimateStepBack),
        transitionState = if (isConfiguration) localBack else routeBack,
        onBack = { currentIntent(ServerSetupIntent.NavigateBack) },
    )
    SourceSelectionEffects(state, strings, sourceListState, processIntent)
    LaunchedEffect(state.mode) { configurationListState.scrollToItem(0) }
    LaunchedEffect(state.step) {
        if (isConfiguration) hasOpenedConfiguration = true
        else {
            withFrameNanos { }
            localBack.reset()
        }
    }
    val handleIntent: (ServerSetupIntent) -> Unit = { intent ->
        if (intent == ServerSetupIntent.NavigateBack && shouldAnimateStepBack) {
            localBack.complete(
                canCommit = {
                    currentState.step == ServerSetupState.Step.CONFIGURE &&
                            currentState.modal == ServerSetupState.Modal.None
                },
                onBack = { currentIntent(intent) },
            )
        } else currentIntent(intent)
    }
    Box(modifier.fillMaxSize()) {
        ServerSetupScaffold(state, handleIntent, strings) { paddingValues ->
            if (state.loadingConfiguration) {
                ServerSetupLoading(
                    modifier = Modifier.padding(paddingValues).fillMaxSize(),
                    text = strings.loadingConfiguration,
                )
            } else {
                ProgrammaticPager(
                    selectedPage = state.step.ordinal,
                    pageCount = ServerSetupState.Step.entries.size,
                    modifier = Modifier.padding(paddingValues).fillMaxSize(),
                    shouldAnimate = !localBack.hasCommittedGesture,
                    backProgress = if (shouldAnimateStepBack && localBack.isActive) localBack.progress else null,
                ) { page ->
                    val step = ServerSetupState.Step.entries[page]
                    val canSendIntent = LocalNavigationInputEnabled.current && step == state.step
                    val pageIntent: (ServerSetupIntent) -> Unit =
                        { if (canSendIntent) currentIntent(it) }
                    if (step == ServerSetupState.Step.SOURCE || isConfiguration || hasOpenedConfiguration) {
                        when (step) {
                            ServerSetupState.Step.SOURCE -> SourceSelectionStep(
                                state, strings, sourceListState, pageIntent, Modifier.fillMaxSize()
                            )

                            ServerSetupState.Step.CONFIGURE -> ConfigurationStep(
                                state,
                                strings,
                                configurationListState,
                                pageIntent,
                                Modifier.fillMaxSize()
                            )
                        }
                    }
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
