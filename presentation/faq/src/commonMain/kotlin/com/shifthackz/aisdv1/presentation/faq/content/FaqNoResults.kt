package com.shifthackz.aisdv1.presentation.faq.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.faq.generated.resources.Res
import com.shifthackz.aisdv1.presentation.faq.generated.resources.faq_no_results
import org.jetbrains.compose.resources.painterResource

/**
 * Shows a compact no-results panel beneath the search toolbar.
 * The complete panel can scroll within half of the viewport above the keyboard.
 *
 * @param isCached Whether the currently searched answers came from the offline snapshot.
 * @param onRetry Requests the latest answers from the cached-copy notice.
 * @param modifier Bounds of the results area below the toolbar.
 * @author Dmitriy Moroz
 */
@Composable
internal fun FaqNoResults(
    isCached: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.padding(horizontal = 16.dp)) {
        val panelMaxHeight = maxHeight / 2
        val imageAspectRatio = 4f / 3f
        val imageHeight = minOf(panelMaxHeight / 2, maxWidth / imageAspectRatio, 112.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = panelMaxHeight)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (imageHeight > 0.dp) {
                Image(
                    painter = painterResource(Res.drawable.faq_no_results),
                    contentDescription = null,
                    modifier = Modifier.size(
                        width = imageHeight * imageAspectRatio,
                        height = imageHeight
                    ),
                )
            }
            Text(
                text = Localization.string("faq_no_results"),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 480.dp).semantics {
                    liveRegion = LiveRegionMode.Polite
                },
            )
            if (isCached) {
                FaqCachedNotice(onRetry = onRetry)
            }
        }
    }
}
