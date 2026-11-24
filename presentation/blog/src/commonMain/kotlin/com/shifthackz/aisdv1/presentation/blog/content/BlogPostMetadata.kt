package com.shifthackz.aisdv1.presentation.blog.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.BlogPost
import com.shifthackz.aisdv1.presentation.components.widget.source.StatusBadge

/**
 * Pairs the publisher's category badge with the article date in the current locale.
 *
 * @param post Post whose publication metadata is displayed.
 * @param modifier Placement of the metadata line.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BlogPostMetadata(post: BlogPost, modifier: Modifier = Modifier) {
    val languageCode = Localization.currentLanguageCode()
    val publicationDate = remember(post.publishedOn, languageCode) {
        formatBlogPostDate(post.publishedOn, languageCode)
    }
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (post.category.isNotBlank()) {
            StatusBadge(post.category, modifier = Modifier.align(Alignment.CenterVertically))
        }
        Text(
            text = publicationDate,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = LocalContentColor.current.copy(alpha = 0.85f),
            modifier = Modifier.align(Alignment.CenterVertically),
        )
    }
}
