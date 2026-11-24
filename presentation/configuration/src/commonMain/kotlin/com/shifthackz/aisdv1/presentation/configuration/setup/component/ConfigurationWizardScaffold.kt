package com.shifthackz.aisdv1.presentation.configuration.setup.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings

/**
 * Shared two-step layout for generation and upscaler configuration.
 * Provider-specific search, actions and form content remain owned by their respective screens.
 *
 * @param title Localized configuration purpose.
 * @param step Selected provider or configuration page.
 * @param strings Shared step captions.
 * @param canNavigateBack Whether an exit or previous step is available.
 * @param onBack Requests navigation through the owning ViewModel.
 * @param modifier Bounds of the full wizard.
 * @param toolbarExtensionHeight Native toolbar space reserved for provider discovery controls.
 * @param toolbarExtension Native discovery controls below the step strip.
 * @param afterToolbar Android discovery controls outside the native toolbar.
 * @param actions Fixed bottom action bar.
 * @param content Pager or page body receiving scaffold insets.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ConfigurationWizardScaffold(
    title: String,
    step: ServerSetupState.Step,
    strings: ServerSetupStrings,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    toolbarExtensionHeight: Dp = 0.dp,
    toolbarExtension: @Composable () -> Unit = {},
    afterToolbar: @Composable () -> Unit = {},
    actions: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    PlatformScaffold(modifier = modifier, canUnderlapToolbar = true, topBar = {
        Column {
            PlatformTopAppBar(
                title = {
                    Text(
                        title,
                        style = platformToolbarTitleStyle(MaterialTheme.typography.headlineMedium)
                    )
                }, navigationIcon = {
                    if (canNavigateBack) ToolbarBackButton(
                        onClick = onBack,
                        icon = Icons.AutoMirrored.Outlined.ArrowBack
                    )
                }, windowInsets = persistentTopAppBarWindowInsets(),
                bottomContentHeight = configurationStepBarHeight() + toolbarExtensionHeight,
                bottomContent = {
                    Column {
                        ConfigurationStepBar(step, strings)
                        toolbarExtension()
                    }
                })
            afterToolbar()
        }
    }, bottomBar = actions, content = content)
}
