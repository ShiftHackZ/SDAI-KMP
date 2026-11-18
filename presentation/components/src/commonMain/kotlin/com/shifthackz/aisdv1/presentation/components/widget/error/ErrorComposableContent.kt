package com.shifthackz.aisdv1.presentation.components.widget.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shows a centered error heading and explanatory message inside a caller-sized surface.
 *
 * @param title Already localized error heading.
 * @param message User-visible explanation of the failure.
 * @param modifier Layout modifiers for the error group.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ErrorComposableContent(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = title,
            fontSize = 20.sp,
        )
        Text(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterHorizontally),
            text = message,
            textAlign = TextAlign.Center,
        )
    }
}
