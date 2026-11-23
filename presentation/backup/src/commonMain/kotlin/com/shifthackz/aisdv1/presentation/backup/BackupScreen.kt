package com.shifthackz.aisdv1.presentation.backup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.core.navigation.router.BackupRouter
import com.shifthackz.aisdv1.presentation.backup.platform.rememberBackupDocumentLauncher
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

/**
 * Binds backup state to native document presentation, operation effects and route disposal.
 * The state-only renderer remains separate from native launchers and the initialized application graph.
 *
 * @param mode Welcome, first-launch restore or Settings tabs to compose.
 * @param router Navigation owner supplied by the application host.
 * @param modifier Placement of the backup screen.
 * @author Dmitriy Moroz
 */
@Composable
fun BackupScreen(
    mode: BackupScreenMode,
    router: BackupRouter,
    modifier: Modifier = Modifier,
) {
    val koin = remember { getKoin() }
    val viewModel = remember(koin, router, mode) {
        koin.get<BackupViewModel> { parametersOf(mode, router) }
    }
    val launcher = rememberBackupDocumentLauncher(
        onPicked = { viewModel.processIntent(BackupIntent.DocumentSelected(it)) },
        onExported = { destination, hasExported, handle ->
            viewModel.processIntent(BackupIntent.ExportFinished(destination, hasExported, handle))
        },
    )
    DisposableEffect(viewModel) { onDispose { viewModel.onRouteDisposed() } }
    MviComponent(
        viewModel = viewModel,
        processEffect = { effect ->
            when (effect) {
                BackupEffect.OpenDocument -> launcher.open()
                is BackupEffect.ExportDocument -> launcher.export(effect.backup)
            }
        },
    ) { state, processIntent ->
        BackupScreenContent(
            mode = mode,
            state = state,
            processIntent = processIntent,
            modifier = modifier
        )
    }
}
