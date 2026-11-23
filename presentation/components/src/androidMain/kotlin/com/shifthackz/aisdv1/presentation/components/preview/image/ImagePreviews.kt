package com.shifthackz.aisdv1.presentation.components.preview.image

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.preview.rememberComponentPreviewBitmap
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryButton
import com.shifthackz.aisdv1.presentation.components.widget.frame.PhoneFrame
import com.shifthackz.aisdv1.presentation.components.widget.image.SampledImageContent
import com.shifthackz.aisdv1.presentation.components.widget.image.ZoomableImage
import com.shifthackz.aisdv1.presentation.components.widget.image.ZoomableImageSource
import com.shifthackz.aisdv1.presentation.components.widget.image.sdaiLogoPainter
import com.shifthackz.aisdv1.presentation.components.widget.imageinput.ImageInputActions
import com.shifthackz.aisdv1.presentation.components.widget.imageinput.ImageInputCard
import com.shifthackz.aisdv1.presentation.components.widget.imageinput.ImagePickerTileButton
import androidx.compose.ui.Modifier

/**
 * Image source cards, action tiles, decoded slot geometry, fit/obscured zoom views, logo and phone frame.
 *
 * @author Dmitriy Moroz
 */
internal class ImagePreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("ImageInputActions · true") {
            ImageInputActions(
                isEnabled = true,
                onGalleryClick = {},
                onCameraClick = {},
                onRandomClick = {},
                onHistoryClick = {})
        },
        ComponentPreviewCase("ImagePickerTileButton · true") {
            ImagePickerTileButton(
                text = "Gallery",
                helpMessage = "Select a source image",
                icon = Icons.Default.Settings,
                isEnabled = true,
                onClick = {})
        },
        ComponentPreviewCase("ImageInputActions · false") {
            ImageInputActions(
                isEnabled = false,
                onGalleryClick = {},
                onCameraClick = {},
                onRandomClick = {},
                onHistoryClick = {})
        },
        ComponentPreviewCase("ImagePickerTileButton · false") {
            ImagePickerTileButton(
                text = "Gallery",
                helpMessage = "Select a source image",
                icon = Icons.Default.Settings,
                isEnabled = false,
                onClick = {})
        },
        ComponentPreviewCase("ImageInputCard · Empty") {
            val bitmap = rememberComponentPreviewBitmap()
            ImageInputCard(
                isPickingImage = false,
                isEnabled = true,
                hasImage = false,
                onGalleryClick = {},
                onCameraClick = {},
                onRandomClick = {},
                onHistoryImageSelected = {},
                isInitiallyExpanded = true
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Source",
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
        },
        ComponentPreviewCase("ImageInputCard · Selected") {
            val bitmap = rememberComponentPreviewBitmap()
            ImageInputCard(
                isPickingImage = false,
                isEnabled = true,
                hasImage = true,
                onGalleryClick = {},
                onCameraClick = {},
                onRandomClick = {},
                onHistoryImageSelected = {},
                isInitiallyExpanded = true
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Source",
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
        },
        ComponentPreviewCase("ImageInputCard · Picking") {
            val bitmap = rememberComponentPreviewBitmap()
            ImageInputCard(
                isPickingImage = true,
                isEnabled = false,
                hasImage = false,
                onGalleryClick = {},
                onCameraClick = {},
                onRandomClick = {},
                onHistoryImageSelected = {},
                isInitiallyExpanded = true
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Source",
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
        },
        ComponentPreviewCase("ImageInputCard · Disabled") {
            val bitmap = rememberComponentPreviewBitmap()
            ImageInputCard(
                isPickingImage = false,
                isEnabled = false,
                hasImage = false,
                onGalleryClick = {},
                onCameraClick = {},
                onRandomClick = {},
                onHistoryImageSelected = {},
                isInitiallyExpanded = true
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Source",
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
        },
        ComponentPreviewCase("ImageInputCard · Collapsed") {
            val bitmap = rememberComponentPreviewBitmap()
            ImageInputCard(
                isPickingImage = false,
                isEnabled = true,
                hasImage = true,
                onGalleryClick = {},
                onCameraClick = {},
                onRandomClick = {},
                onHistoryImageSelected = {},
                isInitiallyExpanded = false
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Source",
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
        },
        ComponentPreviewCase("ZoomableImage · Square · Hidden false") {
            val bitmap = rememberComponentPreviewBitmap(128, 128)
            ZoomableImage(
                source = ZoomableImageSource.Bitmap(bitmap),
                modifier = Modifier.height(260.dp),
                hideImage = false,
                gesturesEnabled = false
            )
        },
        ComponentPreviewCase("ZoomableImage · Square · Hidden true") {
            val bitmap = rememberComponentPreviewBitmap(128, 128)
            ZoomableImage(
                source = ZoomableImageSource.Bitmap(bitmap),
                modifier = Modifier.height(260.dp),
                hideImage = true,
                gesturesEnabled = false
            )
        },
        ComponentPreviewCase("ZoomableImage · Portrait · Hidden false") {
            val bitmap = rememberComponentPreviewBitmap(128, 256)
            ZoomableImage(
                source = ZoomableImageSource.Bitmap(bitmap),
                modifier = Modifier.height(260.dp),
                hideImage = false,
                gesturesEnabled = false
            )
        },
        ComponentPreviewCase("ZoomableImage · Portrait · Hidden true") {
            val bitmap = rememberComponentPreviewBitmap(128, 256)
            ZoomableImage(
                source = ZoomableImageSource.Bitmap(bitmap),
                modifier = Modifier.height(260.dp),
                hideImage = true,
                gesturesEnabled = false
            )
        },
        ComponentPreviewCase("ZoomableImage · Landscape · Hidden false") {
            val bitmap = rememberComponentPreviewBitmap(256, 128)
            ZoomableImage(
                source = ZoomableImageSource.Bitmap(bitmap),
                modifier = Modifier.height(260.dp),
                hideImage = false,
                gesturesEnabled = false
            )
        },
        ComponentPreviewCase("ZoomableImage · Landscape · Hidden true") {
            val bitmap = rememberComponentPreviewBitmap(256, 128)
            ZoomableImage(
                source = ZoomableImageSource.Bitmap(bitmap),
                modifier = Modifier.height(260.dp),
                hideImage = true,
                gesturesEnabled = false
            )
        },
        ComponentPreviewCase("ZoomableImage · Fit width") {
            ZoomableImage(
                source = ZoomableImageSource.Bitmap(
                    rememberComponentPreviewBitmap(
                        128,
                        256
                    )
                ), modifier = Modifier.height(260.dp), fitToWidth = true, gesturesEnabled = false
            )
        },
        ComponentPreviewCase("SampledImageContent · Empty") {
            SampledImageContent(image = null, modifier = Modifier.size(180.dp))
        },
        ComponentPreviewCase("SampledImageContent · Fit") {
            SampledImageContent(
                image = rememberComponentPreviewBitmap(256, 128),
                modifier = Modifier.size(180.dp)
            )
        },
        ComponentPreviewCase("SampledImageContent · Crop") {
            SampledImageContent(
                image = rememberComponentPreviewBitmap(128, 256),
                modifier = Modifier.size(180.dp),
                contentScale = ContentScale.Crop
            )
        },
        ComponentPreviewCase("PhoneFrame") {
            PhoneFrame(Modifier.width(240.dp)) {
                Column(
                    Modifier.height(320.dp).padding(16.dp)
                ) { Text("SDAI"); PrimaryButton(onClick = {}) { Text("Continue") } }
            }
        },
        ComponentPreviewCase("Logo") {
            Image(
                painter = sdaiLogoPainter(),
                contentDescription = "SDAI",
                modifier = Modifier.size(160.dp)
            )
        },
    ),
)

/**
 * Renders named image states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 480)
@Preview(name = "Dark", widthDp = 400, heightDp = 480, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 480, fontScale = 1.5f)
@Composable
private fun ImagePreview(@PreviewParameter(ImagePreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
