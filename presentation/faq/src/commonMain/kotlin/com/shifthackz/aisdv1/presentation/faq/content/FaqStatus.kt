package com.shifthackz.aisdv1.presentation.faq.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.extensions.shimmer
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface
import com.shifthackz.aisdv1.presentation.faq.generated.resources.Res
import com.shifthackz.aisdv1.presentation.faq.generated.resources.faq_load_error
import org.jetbrains.compose.resources.painterResource

/**
 * Reserves space for the search field and question cards while the online attempt runs.
 *
 * @param modifier Placement of the placeholders.
 * @author Dmitriy Moroz
 */
@Composable
internal fun FaqLoading(modifier: Modifier = Modifier) {
    val loading = Localization.string("loading")
    Column(
        modifier = modifier.padding(16.dp).semantics {
            contentDescription = loading
            liveRegion = LiveRegionMode.Polite
        },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(6) {
            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).shimmer())
        }
    }
}

/**
 * Centers the connection illustration, error message, and Retry action in a scrollable layout.
 *
 * @param onRetry Starts a new fetch.
 * @param modifier Placement of the error state.
 * @author Dmitriy Moroz
 */
@Composable
internal fun FaqLoadError(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                painter = painterResource(Res.drawable.faq_load_error),
                contentDescription = null,
                modifier = Modifier.widthIn(max = 288.dp).fillMaxWidth().aspectRatio(4f / 3f),
            )
            Text(
                text = Localization.string("faq_load_error"),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 480.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
            TextButton(onClick = onRetry) { Text(Localization.string("retry")) }
        }
    }
}

/**
 * Indicates when answers came from the offline snapshot and allows refreshing them.
 *
 * @param onRetry Starts a new fetch.
 * @param modifier Placement of the status surface.
 * @author Dmitriy Moroz
 */
@Composable
internal fun FaqCachedNotice(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    PlatformSurface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = LocalContentColor.current,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            Text(
                text = Localization.string("faq_cached"),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            TextButton(onClick = onRetry, modifier = Modifier.align(Alignment.End)) {
                Text(Localization.string("retry"))
            }
        }
    }
}
