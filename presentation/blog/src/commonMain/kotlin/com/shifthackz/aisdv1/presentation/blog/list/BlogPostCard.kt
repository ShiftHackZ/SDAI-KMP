package com.shifthackz.aisdv1.presentation.blog.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifthackz.aisdv1.domain.entity.BlogPost
import com.shifthackz.aisdv1.presentation.blog.content.BlogCover
import com.shifthackz.aisdv1.presentation.blog.content.BlogPostMetadata
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface

/**
 * Presents one article as a single tappable list card.
 *
 * @param post Published title, excerpt, date, and cover.
 * @param onOpen Opens the native detail route.
 * @param modifier Placement of the card within the feed.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BlogPostCard(
    post: BlogPost,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlatformSurface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = LocalContentColor.current,
    ) {
        Column(modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onOpen)) {
            BlogCover(url = post.coverUrl)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BlogPostMetadata(post)
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = post.summary,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                )
            }
        }
    }
}
