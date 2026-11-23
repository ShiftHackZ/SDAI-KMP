package com.shifthackz.aisdv1.presentation.components.preview.source

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.widget.source.BetaBadge
import com.shifthackz.aisdv1.presentation.components.widget.source.CurrentProviderCard
import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Every provider card on Android and iOS identities, using the shared platform-aware icon mapping.
 *
 * @author Dmitriy Moroz
 */
internal class SourcePreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("CurrentProviderCard · AUTOMATIC1111 · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.AUTOMATIC1111,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · AUTOMATIC1111 · IOS") {
            CurrentProviderCard(
                source = ServerSource.AUTOMATIC1111,
                platform = Platform.IOS,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · SWARM_UI · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.SWARM_UI,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · SWARM_UI · IOS") {
            CurrentProviderCard(
                source = ServerSource.SWARM_UI,
                platform = Platform.IOS,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · OPEN_AI_COMPATIBLE · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.OPEN_AI_COMPATIBLE,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · OPEN_AI_COMPATIBLE · IOS") {
            CurrentProviderCard(
                source = ServerSource.OPEN_AI_COMPATIBLE,
                platform = Platform.IOS,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · EASY_DIFFUSION · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.EASY_DIFFUSION,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · EASY_DIFFUSION · IOS") {
            CurrentProviderCard(
                source = ServerSource.EASY_DIFFUSION,
                platform = Platform.IOS,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · LOCAL_MICROSOFT_ONNX · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.LOCAL_MICROSOFT_ONNX,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · LOCAL_MICROSOFT_ONNX · IOS") {
            CurrentProviderCard(
                source = ServerSource.LOCAL_MICROSOFT_ONNX,
                platform = Platform.IOS,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · LOCAL_GOOGLE_MEDIA_PIPE · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · LOCAL_GOOGLE_MEDIA_PIPE · IOS") {
            CurrentProviderCard(
                source = ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
                platform = Platform.IOS,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · LOCAL_STABLE_DIFFUSION_CPP · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · LOCAL_STABLE_DIFFUSION_CPP · IOS") {
            CurrentProviderCard(
                source = ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
                platform = Platform.IOS,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · LOCAL_MNN · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.LOCAL_MNN,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · LOCAL_MNN · IOS") {
            CurrentProviderCard(
                source = ServerSource.LOCAL_MNN,
                platform = Platform.IOS,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · LOCAL_APPLE_CORE_ML · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.LOCAL_APPLE_CORE_ML,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · LOCAL_APPLE_CORE_ML · IOS") {
            CurrentProviderCard(
                source = ServerSource.LOCAL_APPLE_CORE_ML,
                platform = Platform.IOS,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · LOCAL_APPLE_BONSAI · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.LOCAL_APPLE_BONSAI,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · LOCAL_APPLE_BONSAI · IOS") {
            CurrentProviderCard(
                source = ServerSource.LOCAL_APPLE_BONSAI,
                platform = Platform.IOS,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · HORDE · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.HORDE,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · HORDE · IOS") {
            CurrentProviderCard(source = ServerSource.HORDE, platform = Platform.IOS, onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · HUGGING_FACE · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.HUGGING_FACE,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · HUGGING_FACE · IOS") {
            CurrentProviderCard(
                source = ServerSource.HUGGING_FACE,
                platform = Platform.IOS,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · OPEN_AI · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.OPEN_AI,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · OPEN_AI · IOS") {
            CurrentProviderCard(
                source = ServerSource.OPEN_AI,
                platform = Platform.IOS,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · OPEN_AI_OAUTH · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.OPEN_AI_OAUTH,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · OPEN_AI_OAUTH · IOS") {
            CurrentProviderCard(
                source = ServerSource.OPEN_AI_OAUTH,
                platform = Platform.IOS,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · STABILITY_AI · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.STABILITY_AI,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · STABILITY_AI · IOS") {
            CurrentProviderCard(
                source = ServerSource.STABILITY_AI,
                platform = Platform.IOS,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · FAL_AI · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.FAL_AI,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · FAL_AI · IOS") {
            CurrentProviderCard(source = ServerSource.FAL_AI, platform = Platform.IOS, onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · ARLI_AI · ANDROID") {
            CurrentProviderCard(
                source = ServerSource.ARLI_AI,
                platform = Platform.ANDROID,
                onClick = {})
        },
        ComponentPreviewCase("CurrentProviderCard · ARLI_AI · IOS") {
            CurrentProviderCard(
                source = ServerSource.ARLI_AI,
                platform = Platform.IOS,
                onClick = {})
        },
        ComponentPreviewCase("BetaBadge") {
            BetaBadge()
        },
    ),
)

/**
 * Renders named source states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 320)
@Preview(name = "Dark", widthDp = 400, heightDp = 320, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 440, fontScale = 1.5f)
@Composable
private fun SourcePreview(@PreviewParameter(SourcePreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
