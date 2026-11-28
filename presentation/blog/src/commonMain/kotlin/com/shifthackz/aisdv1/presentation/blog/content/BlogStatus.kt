package com.shifthackz.aisdv1.presentation.blog.content

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
import com.shifthackz.aisdv1.presentation.components.widget.action.TextActionButton
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
import com.shifthackz.aisdv1.presentation.blog.generated.resources.Res
import com.shifthackz.aisdv1.presentation.blog.generated.resources.blog_load_error
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface
import org.jetbrains.compose.resources.painterResource

/**
 * Reserves room for the feed's imagery and text while content is loading.
 *
 * @param modifier Placement of the loading placeholders.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BlogLoading(modifier: Modifier = Modifier) {
    val loading = Localization.string("loading")
    Column(
        modifier = modifier.padding(16.dp).semantics {
            contentDescription = loading
            liveRegion = LiveRegionMode.Polite
        },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(16.dp)).shimmer())
        Box(Modifier.fillMaxWidth(0.8f).height(28.dp).clip(RoundedCornerShape(8.dp)).shimmer())
        Box(Modifier.fillMaxWidth(0.6f).height(16.dp).clip(RoundedCornerShape(8.dp)).shimmer())
    }
}

/**
 * Pairs the feed or article load failure with decorative artwork and a retry action.
 *
 * @param messageKey Localized reason the feed or article could not be displayed.
 * @param onRetry Requests another download.
 * @param modifier Placement within the screen.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BlogLoadError(messageKey: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                painter = painterResource(Res.drawable.blog_load_error),
                contentDescription = null,
                modifier = Modifier.widthIn(max = 288.dp).fillMaxWidth().aspectRatio(4f / 3f),
            )
            Text(
                text = Localization.string(messageKey),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            TextActionButton(onClick = onRetry) { Text(Localization.string("retry")) }
        }
    }
}

/**
 * Explains why a saved feed is shown and offers a fresh download.
 *
 * @param onRetry Requests an online refresh.
 * @param modifier Placement among the feed rows.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BlogCachedNotice(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    PlatformSurface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = LocalContentColor.current,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            Text(
                text = Localization.string("blog_cached"),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            TextActionButton(onClick = onRetry, modifier = Modifier.align(Alignment.End)) {
                Text(Localization.string("retry"))
            }
        }
    }
}
