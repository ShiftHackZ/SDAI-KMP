package com.shifthackz.aisdv1.presentation.backup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.backup.dialog.BackupDialogs
import com.shifthackz.aisdv1.presentation.backup.welcome.RestoreWelcomeScreen
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryActionButton
import com.shifthackz.aisdv1.presentation.components.widget.action.ScreenActionBar
import kotlinx.coroutines.flow.drop

/**
 * Hosts reusable backup content with configuration-aligned chrome and operation dialogs.
 * @param mode Settings tabs or first-launch recovery context.
 * @param state Nonsecret inventory, selection and operation feedback.
 * @param processIntent Caller-owned MVI event sink.
 * @param modifier Root screen placement.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    mode: BackupScreenMode,
    state: BackupState,
    processIntent: (BackupIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEnabled = !state.isBusy && !state.isPicking && !state.isExporting
    if (mode == BackupScreenMode.WELCOME) {
        RestoreWelcomeScreen(
            isEnabled = isEnabled,
            onRestore = { processIntent(BackupIntent.BeginInitialRestore) },
            onContinue = { processIntent(BackupIntent.Continue) },
            modifier = modifier,
        )
    } else {
        val page = if (state.isRestoreTab) state.restore else state.create
        val pager = rememberPagerState(initialPage = if (state.isRestoreTab) 1 else 0) { 2 }
        val isRestoreSelected by rememberUpdatedState(state.isRestoreTab)
        val selectedPage = if (state.isRestoreTab) 1 else 0
        val hasSettledPage = mode != BackupScreenMode.SETTINGS ||
                (!pager.isScrollInProgress && pager.currentPage == selectedPage && pager.currentPageOffsetFraction == 0f)
        LaunchedEffect(state.isRestoreTab) {
            if (mode == BackupScreenMode.SETTINGS) {
                pager.animateScrollToPage(selectedPage)
            }
        }
        LaunchedEffect(pager) {
            if (mode == BackupScreenMode.SETTINGS) {
                snapshotFlow { pager.settledPage }.drop(1).collect { settledPage ->
                    val shouldRestore = settledPage == 1
                    if (shouldRestore != isRestoreSelected) processIntent(
                        BackupIntent.SelectTab(
                            shouldRestore
                        )
                    )
                }
            }
        }
        val isFinished =
            if (state.isRestoreTab) state.result?.hasFailed == false else state.hasExported
        Scaffold(
            modifier = modifier,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            Localization.string("backup_title"),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { processIntent(BackupIntent.NavigateBack) },
                            enabled = isEnabled
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                Localization.string("action_back")
                            )
                        }
                    },
                    windowInsets = persistentTopAppBarWindowInsets(),
                )
            },
            bottomBar = {
                ScreenActionBar {
                    PrimaryActionButton(
                        onClick = { processIntent(if (isFinished) BackupIntent.NavigateBack else BackupIntent.Submit) },
                        isEnabled = isEnabled && hasSettledPage && (isFinished || page.selection.categories.isNotEmpty()),
                    ) {
                        Text(Localization.string(if (isFinished) "backup_done" else if (state.isRestoreTab) "backup_restore" else "backup_create"))
                    }
                }
            },
        ) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                if (mode == BackupScreenMode.SETTINGS) {
                    TabRow(selectedTabIndex = selectedPage) {
                        Tab(
                            selected = !state.isRestoreTab,
                            onClick = { processIntent(BackupIntent.SelectTab(false)) },
                            enabled = isEnabled,
                            text = { Text(Localization.string("backup_create_tab")) },
                        )
                        Tab(
                            selected = state.isRestoreTab,
                            onClick = { processIntent(BackupIntent.SelectTab(true)) },
                            enabled = isEnabled,
                            text = { Text(Localization.string("backup_restore_tab")) },
                        )
                    }
                    HorizontalPager(
                        state = pager,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        userScrollEnabled = isEnabled,
                        key = { it },
                    ) { pageIndex ->
                        val isRestorePage = pageIndex == 1
                        BackupPageContent(
                            isRestore = isRestorePage,
                            page = if (isRestorePage) state.restore else state.create,
                            isEnabled = isEnabled && hasSettledPage,
                            canEditSelection = if (isRestorePage) !state.isRestoreSelectionLocked else !state.hasPendingExport,
                            processIntent = processIntent,
                        )
                    }
                } else {
                    BackupPageContent(
                        isRestore = true,
                        page = state.restore,
                        isEnabled = isEnabled,
                        canEditSelection = !state.isRestoreSelectionLocked,
                        processIntent = processIntent,
                    )
                }
            }
        }
    }
    BackupDialogs(state, processIntent)
}
