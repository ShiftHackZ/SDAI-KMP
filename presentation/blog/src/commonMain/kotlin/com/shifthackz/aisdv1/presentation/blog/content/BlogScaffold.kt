package com.shifthackz.aisdv1.presentation.blog.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle

/**
 * Gives the blog destinations the same platform toolbar and safe areas as the FAQ.
 *
 * @param onBack Returns to the previous route.
 * @param modifier Layout of the full-screen scaffold.
 * @param actions Optional trailing toolbar controls.
 * @param content Screen body, responsible for applying the toolbar inset to its scroll content.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BlogScaffold(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    PlatformScaffold(
        modifier = modifier,
        canUnderlapToolbar = true,
        topBar = {
            PlatformTopAppBar(
                title = {
                    Text(
                        text = Localization.string("blog_title"),
                        style = platformToolbarTitleStyle(MaterialTheme.typography.headlineMedium),
                    )
                },
                navigationIcon = { ToolbarBackButton(onClick = onBack) },
                actions = actions,
                windowInsets = persistentTopAppBarWindowInsets(),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
            content = content,
        )
    }
}
