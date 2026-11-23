package com.shifthackz.aisdv1.presentation.web

import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.source.getName

/**
 * Displays provider configuration and page-loading feedback around the supplied browser viewport.
 * The caller owns browser lifecycle, loading callbacks and back-navigation policy.
 *
 * @param state Provider title and configuration-loading state.
 * @param isPageLoading Whether the embedded page is still loading.
 * @param onNavigateBack Requests navigation according to the caller's browser-history policy.
 * @param modifier Layout modifiers for the WebUI scaffold.
 * @param content Embedded browser or design-time viewport, receiving its layout modifier.
 * @author Dmitriy Moroz
 */
@Composable
internal fun WebUiContent(
    state: WebUiState,
    isPageLoading: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    PlatformScaffold(
        topBar = {
            PlatformTopAppBar(
                title = {
                    Text(
                        text = state.source.takeIf { !state.loading }?.getName() ?: "",
                        style = platformToolbarTitleStyle(MaterialTheme.typography.headlineMedium),
                    )
                },
                navigationIcon = {
                    ToolbarBackButton(
                        onClick = { onNavigateBack() }, icon = Icons.AutoMirrored.Outlined.ArrowBack
                    )
                },
                windowInsets = persistentTopAppBarWindowInsets(),
            )
        },
        contentColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) { paddingValues ->
        AnimatedContent(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            targetState = !state.loading,
            label = "web_ui_root_state_animation",
        ) { contentVisible ->
            if (contentVisible) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    verticalArrangement = Arrangement.Top,
                ) {
                    AnimatedVisibility(visible = isPageLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = AlertDialogDefaults.iconContentColor,
                        )
                    }
                    content(Modifier.fillMaxSize())
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                    )
                }
            }
        }
    }
}
