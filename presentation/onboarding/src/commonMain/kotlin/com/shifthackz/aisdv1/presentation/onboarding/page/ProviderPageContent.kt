package com.shifthackz.aisdv1.presentation.onboarding.page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Cloud
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.onboarding.buildOnBoardingText
import com.shifthackz.aisdv1.presentation.onboarding.onBoardingDensity
import com.shifthackz.aisdv1.presentation.onboarding.onBoardingPhoneAspectRatio
import com.shifthackz.aisdv1.presentation.onboarding.onBoardingPhoneWidthFraction
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.configuration.setup.mappers.mapToUi
import com.shifthackz.aisdv1.presentation.components.widget.frame.PhoneFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Introduces cloud providers through a noninteractive setup illustration.
 * Visible pages cycle the selected provider; design previews retain the initial selection.
 *
 * @param modifier Layout bounds for the onboarding page.
 * @param isPageVisible Whether the pager currently exposes this page and its animation may run.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ProviderPageContent(
    modifier: Modifier = Modifier,
    isPageVisible: Boolean = false,
) = Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    val scope = rememberCoroutineScope()
    val shouldAnimate = isPageVisible && !LocalInspectionMode.current
    val strings = ServerSetupStrings()
    val previewSources = remember {
        listOf(
            ServerSource.HUGGING_FACE,
            ServerSource.OPEN_AI,
            ServerSource.OPEN_AI_COMPATIBLE,
            ServerSource.STABILITY_AI,
            ServerSource.ARLI_AI,
        )
    }
    var selectedSource by remember { mutableStateOf(ServerSource.STABILITY_AI) }

    Spacer(modifier = Modifier.weight(1f))
    Text(
        text = buildOnBoardingText(Localization.string("on_boarding_page_provider_title")),
        fontSize = 24.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight(450),
    )
    Spacer(modifier = Modifier.weight(1f))
    PhoneFrame(
        modifier = Modifier.fillMaxWidth(onBoardingPhoneWidthFraction),
    ) {
        CompositionLocalProvider(LocalDensity provides onBoardingDensity) {
            ProviderPreviewScreen(
                modifier = Modifier.aspectRatio(onBoardingPhoneAspectRatio),
                strings = strings,
                sources = previewSources,
                selectedSource = selectedSource,
            )
        }
    }
    Spacer(modifier = Modifier.weight(1f))

    DisposableEffect(shouldAnimate) {
        val job = scope.launch {
            var index = previewSources.indexOf(selectedSource).coerceAtLeast(0)
            while (shouldAnimate) {
                delay(1200.milliseconds)
                index = (index + 1) % previewSources.size
                selectedSource = previewSources[index]
            }
        }
        onDispose { job.cancel() }
    }
}

/**
 * Draws the fixed setup illustration without provider discovery, credentials or connection actions.
 *
 * @param strings Localized setup labels and provider descriptions.
 * @param sources Ordered cloud providers shown inside the phone.
 * @param selectedSource Provider highlighted by the page's illustration animation.
 * @param modifier Layout bounds inside the phone frame.
 * @author Dmitriy Moroz
 */
@Composable
private fun ProviderPreviewScreen(
    strings: ServerSetupStrings,
    sources: List<ServerSource>,
    selectedSource: ServerSource,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = strings.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
            )
            ProviderPreviewStepBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                strings = strings,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                sources.forEach { source ->
                    ProviderPreviewItem(
                        source = source,
                        strings = strings,
                        isSelected = source == selectedSource,
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            PrimaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                onClick = {},
            ) {
                Text(
                    text = strings.next,
                    color = LocalContentColor.current,
                )
            }
        }
    }
}

/**
 * Shows source selection as the active step and provider configuration as the next step.
 *
 * @param strings Localized names of the two setup steps.
 * @param modifier Width and placement inside the illustration.
 * @author Dmitriy Moroz
 */
@Composable
private fun ProviderPreviewStepBar(
    strings: ServerSetupStrings,
    modifier: Modifier = Modifier,
) {
    val circleSize = 22.dp
    val lineHeight = 3.dp
    val accent = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.48f)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
    ) {
        PreviewStep(
            modifier = Modifier.weight(1f),
            label = strings.sourceTitle,
            isActive = true,
            icon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = accent,
                )
            },
        )
        Box(
            modifier = Modifier
                .padding(top = circleSize / 2 - lineHeight / 2)
                .weight(1f)
                .height(lineHeight)
                .background(inactive),
        )
        PreviewStep(
            modifier = Modifier.weight(1f),
            label = strings.configureTitle,
            isActive = false,
            icon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Default.Circle,
                    contentDescription = null,
                    tint = inactive,
                )
            },
        )
    }
}

/**
 * Displays one illustrated setup step with its icon and active/inactive treatment.
 *
 * @param label Localized name displayed beneath the indicator.
 * @param isActive Whether this step is highlighted as current.
 * @param modifier Size and parent row placement of this step.
 * @param icon Decorative content centered inside the circular indicator.
 * @author Dmitriy Moroz
 */
@Composable
private fun PreviewStep(
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.48f)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isActive) accent.copy(alpha = 0.28f) else inactive)
                .border(
                    width = 1.dp,
                    color = accent,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

/**
 * Shows a provider's name, description and capability tags as a noninteractive sample card.
 *
 * @param source Provider whose illustrative metadata is displayed.
 * @param strings Localized provider names and descriptions.
 * @param isSelected Whether this provider has the accent outline.
 * @param modifier Placement in the provider illustration's column.
 * @author Dmitriy Moroz
 */
@Composable
private fun ProviderPreviewItem(
    source: ServerSource,
    strings: ServerSetupStrings,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = source.previewTitle(strings),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = source.previewSubtitle(strings),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.W500,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            FlowRow(
                modifier = Modifier.padding(top = 4.dp),
            ) {
                source.featureTags.take(3).forEach { tag ->
                    Text(
                        modifier = Modifier
                            .padding(end = 4.dp, top = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceTint,
                                shape = RoundedCornerShape(4.dp),
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        text = tag.mapToUi(),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * Resolves the illustrative cloud-provider title from the current bundled localization.
 *
 * @param strings Setup labels used by the embedded provider illustration.
 * @author Dmitriy Moroz
 */
private fun ServerSource.previewTitle(strings: ServerSetupStrings): String = when (this) {
    ServerSource.HUGGING_FACE -> strings.huggingFaceTitle
    ServerSource.OPEN_AI_OAUTH -> Localization.string("srv_type_open_ai_oauth")
    ServerSource.OPEN_AI_COMPATIBLE -> Localization.string("srv_type_open_ai_compatible")
    ServerSource.OPEN_AI -> strings.openAiTitle
    ServerSource.STABILITY_AI -> strings.stabilityTitle
    ServerSource.ARLI_AI -> strings.arliAiTitle
    else -> strings.automaticTitle
}

/**
 * Resolves the short provider explanation displayed inside the limited phone illustration.
 *
 * @param strings Setup descriptions used by the embedded provider illustration.
 * @author Dmitriy Moroz
 */
private fun ServerSource.previewSubtitle(strings: ServerSetupStrings): String = when (this) {
    ServerSource.HUGGING_FACE -> strings.huggingFaceSubtitle
    ServerSource.OPEN_AI_OAUTH -> Localization.string("open_ai_oauth_help")
    ServerSource.OPEN_AI_COMPATIBLE -> Localization.string("open_ai_compatible_help")
    ServerSource.OPEN_AI -> strings.openAiSubtitle
    ServerSource.STABILITY_AI -> strings.stabilitySubtitle
    ServerSource.ARLI_AI -> strings.arliAiSubtitle
    else -> strings.automaticSubtitle
}
