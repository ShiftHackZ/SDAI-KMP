package com.shifthackz.aisdv1.presentation.civitai.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.civitai.generated.resources.Res
import com.shifthackz.aisdv1.presentation.civitai.generated.resources.civitai_history_empty
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import org.jetbrains.compose.resources.painterResource

/**
 * Centers the first-upload invitation in the available history screen, matching Backup welcome artwork.
 * Content can scroll on compact displays and with enlarged text without clipping the illustration or message.
 *
 * @param modifier Full available content area, including the caller's scaffold padding.
 * @author Dmitriy Moroz
 */
@Composable
internal fun CivitaiHistoryEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(top = LocalToolbarContentInset.current)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.civitai_history_empty),
            contentDescription = null,
            modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth().aspectRatio(1.25f),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = Localization.string("civitai_history_empty"),
            modifier = Modifier.widthIn(max = 480.dp).padding(horizontal = 24.dp),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
    }
}
