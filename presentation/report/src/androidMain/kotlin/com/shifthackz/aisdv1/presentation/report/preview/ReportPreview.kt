package com.shifthackz.aisdv1.presentation.report.preview

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState
import com.shifthackz.aisdv1.presentation.report.ReportScreenContent
import com.shifthackz.aisdv1.presentation.report.ReportState
import com.shifthackz.aisdv1.presentation.report.modal.ReportErrorDialog
import com.shifthackz.aisdv1.presentation.report.reportScreenStrings
import com.shifthackz.aisdv1.presentation.report.toContentState

/**
 * Shows the real report scaffold and error dialog using bundled sample pixels and inert event sinks.
 * The thumbnail is decoded synchronously from the tiny fixture without starting the gallery loader.
 *
 * @param state Fixed report draft and submission outcome to display.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ReportPreview(state: ReportState) {
    val image = remember(state.imageBase64) {
        state.imageBase64.takeIf(String::isNotBlank)?.let { base64 ->
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = isSystemInDarkTheme(),
            shouldUseSystemColorPalette = false,
        ),
        applySystemBars = false,
    ) {
        val strings = reportScreenStrings()
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            ReportScreenContent(
                state = state.toContentState(),
                strings = strings,
                onNavigateBack = {},
                onSubmit = {},
                onTextChange = {},
                onReasonChange = {},
                modifier = Modifier.fillMaxSize(),
                imageContent = { imageModifier ->
                    if (image == null) {
                        Box(imageModifier)
                    } else {
                        Image(
                            bitmap = image,
                            contentDescription = strings.imageContentDescription,
                            modifier = imageModifier,
                            contentScale = ContentScale.Crop,
                        )
                    }
                },
            )
            ReportErrorDialog(error = state.error, onDismissRequest = {})
        }
    }
}
