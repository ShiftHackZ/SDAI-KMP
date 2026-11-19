package com.shifthackz.aisdv1.presentation.backup.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.backup.generated.resources.Res
import com.shifthackz.aisdv1.presentation.backup.generated.resources.backup_welcome
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryActionButton
import com.shifthackz.aisdv1.presentation.components.widget.action.ScreenActionBar
import com.shifthackz.aisdv1.presentation.components.widget.action.SecondaryActionButton
import org.jetbrains.compose.resources.painterResource

/**
 * Invites first-launch recovery with branded artwork and the same pinned action geometry as setup.
 * @param isEnabled Whether a durable navigation transition can start.
 * @param onRestore Opens restore-only recovery.
 * @param onContinue Continues through mandatory provider setup.
 * @param modifier Root full-screen layout.
 * @author Dmitriy Moroz
 */
@Composable
internal fun RestoreWelcomeScreen(
    isEnabled: Boolean,
    onRestore: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Spacer(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(persistentTopAppBarWindowInsets())
            )
        },
        bottomBar = {
            ScreenActionBar(containerColor = MaterialTheme.colorScheme.surface) {
                SecondaryActionButton(onClick = onContinue, isEnabled = isEnabled) {
                    Text(Localization.string("backup_continue"))
                }
                PrimaryActionButton(onClick = onRestore, isEnabled = isEnabled) {
                    Text(Localization.string("backup_restore_from_file"))
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(Res.drawable.backup_welcome),
                contentDescription = null,
                modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth().aspectRatio(1.25f),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                Localization.string("backup_welcome_title"),
                modifier = Modifier.widthIn(max = 480.dp),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                Localization.string("backup_welcome_description"),
                modifier = Modifier.widthIn(max = 480.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
