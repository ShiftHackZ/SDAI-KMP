package com.shifthackz.aisdv1.presentation.components.widget.item

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.extensions.shimmer

/**
 * Draws a section title between gradient dividers, retaining layout while loading.
 *
 * @param text Already localized section heading.
 * @param modifier Layout modifiers for the header.
 * @param loading Replaces title content with a shimmer and animates the divider placeholders.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun SettingsHeaderContent(
    text: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = true,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            @Composable
            fun HeaderDivider(modifier: Modifier = Modifier, isInverse: Boolean = false) {
                val brush = Brush.horizontalGradient(
                    buildList {
                        if (isInverse) {
                            add(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                            add(LocalContentColor.current.copy(alpha = 0.2f))
                            add(Color.Transparent)
                        } else {
                            add(Color.Transparent)
                            add(LocalContentColor.current.copy(alpha = 0.2f))
                            add(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        }
                    },
                )
                Spacer(
                    modifier = modifier
                        .height(1.5.dp)
                        .background(brush, RoundedCornerShape(2.dp))
                        .then(if (loading) Modifier.shimmer() else Modifier),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            HeaderDivider(modifier = Modifier.weight(1f))
            AnimatedContent(
                targetState = !loading,
                label = "settings_header",
                transitionSpec = { fadeIn() togetherWith fadeOut() },
            ) { contentVisible ->
                if (contentVisible) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .alpha(if (loading) 0f else 1f),
                        text = text,
                        style = MaterialTheme.typography.headlineSmall,
                        color = LocalContentColor.current,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.33f)
                            .padding(horizontal = 12.dp)
                            .defaultMinSize(minHeight = 24.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .shimmer(),
                    )
                }
            }
            HeaderDivider(modifier = Modifier.weight(1f), isInverse = true)
            Spacer(modifier = Modifier.width(12.dp))
        }
    }
}
