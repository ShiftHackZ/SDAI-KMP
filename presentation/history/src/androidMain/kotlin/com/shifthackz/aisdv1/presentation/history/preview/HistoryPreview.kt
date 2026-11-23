package com.shifthackz.aisdv1.presentation.history.preview

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState
import com.shifthackz.aisdv1.presentation.history.HistoryScreenContent
import com.shifthackz.aisdv1.presentation.history.HistoryStrings
import com.shifthackz.aisdv1.presentation.history.HistoryThumbnail

/**
 * Shows the real history scaffold using fixed sample pixels or the production placeholder.
 * Thumbnail geometry remains controlled by the history card. No ViewModel, Koin, gallery reader
 * or asynchronous image-cache request is started, and navigation/refresh callbacks are inert.
 *
 * @param scenario Fixed metadata, read status and unavailable thumbnail identities.
 * @author Dmitriy Moroz
 */
@Composable
internal fun HistoryPreview(scenario: HistoryPreviewCase) {
    val image = remember {
        val bytes = Base64.decode(HistoryPreviewFixtures.ImageBase64, Base64.DEFAULT)
        requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)).asImageBitmap()
    }
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = isSystemInDarkTheme(),
            shouldUseSystemColorPalette = false,
        ),
        applySystemBars = false,
    ) {
        val strings = HistoryStrings()
        HistoryScreenContent(
            state = scenario.state,
            processIntent = {},
            modifier = Modifier.fillMaxSize(),
            strings = strings,
            imageContent = { item, imageModifier ->
                if (item.id in scenario.unavailableImageIds) {
                    HistoryThumbnail(
                        item = item,
                        imageUnavailable = strings.imageUnavailable,
                        modifier = imageModifier,
                    )
                } else {
                    Image(
                        bitmap = image,
                        contentDescription = null,
                        modifier = imageModifier,
                        contentScale = ContentScale.Crop,
                    )
                }
            },
        )
    }
}
