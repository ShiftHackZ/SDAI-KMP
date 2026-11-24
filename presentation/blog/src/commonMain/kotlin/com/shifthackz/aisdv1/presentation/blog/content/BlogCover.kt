package com.shifthackz.aisdv1.presentation.blog.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.shifthackz.aisdv1.core.extensions.shimmer
import com.shifthackz.aisdv1.presentation.blog.generated.resources.Res
import com.shifthackz.aisdv1.presentation.blog.generated.resources.blog_cover_placeholder
import org.jetbrains.compose.resources.painterResource

/**
 * Keeps the cover at 16:9, shimmers during loading, and shows bundled artwork when the cover is unavailable.
 *
 * @param url Website asset URL.
 * @param modifier Layout and clipping of the cover.
 * @param description Published alternative text, or null when the cover is decorative.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BlogCover(
    url: String,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    val coverUrl = url.takeIf { it.isNotBlank() }
    var isLoading by remember(coverUrl) { mutableStateOf(coverUrl != null) }
    val unavailableCover = painterResource(Res.drawable.blog_cover_placeholder)
    Box(
        modifier = modifier.fillMaxWidth().aspectRatio(16f / 9f)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (isLoading) {
            Box(Modifier.matchParentSize().shimmer())
        }
        AsyncImage(
            model = coverUrl,
            contentDescription = description,
            error = unavailableCover,
            contentScale = ContentScale.Crop,
            onLoading = { isLoading = coverUrl != null },
            onSuccess = { isLoading = false },
            onError = { isLoading = false },
            modifier = Modifier.matchParentSize(),
        )
    }
}
