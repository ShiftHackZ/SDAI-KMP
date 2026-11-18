package com.shifthackz.aisdv1.presentation.configuration.setup.form.local

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.extensions.shimmer

/**
 * Reserves three rounded model rows while an initially empty catalog is loading.
 * @param modifier Layout modifiers for the placeholder list.
 * @author Dmitriy Moroz
 */
@Composable
internal fun LocalModelShimmerItems(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) {
            Box(Modifier.fillMaxWidth().height(68.dp).clip(RoundedCornerShape(16.dp)).shimmer())
        }
    }
}
