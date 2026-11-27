package com.shifthackz.aisdv1.presentation.generation.upscaler

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.domain.entity.UpscalerImageSize

/**
 * Shared upscaler controls for standalone execution and the optional generation final stage.
 * Optional provider and parameter cards expand and collapse together below the enable switch.
 * User activation lets the owning screen follow the expanding controls with its scroll position.
 *
 * @param state Configuration/catalog state observed by the application composition root.
 * @param configuration Captured model selection; null disables the optional pipeline stage.
 * @param onConfigurationChange Updates the owning screen's immutable submission inputs.
 * @param onConfigure Opens independent two-step provider/model configuration.
 * @param modifier Placement of the form surface.
 * @param isEnabled Whether the owning screen currently accepts edits.
 * @param isStandalone Whether upscaling is mandatory rather than an optional stage.
 * @param outputSize Predicted standalone output dimensions; null leaves dimension fields empty.
 * @param onRevealControls Follows user-triggered expansion; the supplied predicate remains true until expansion settles.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UpscalerForm(
    state: UpscalerFormState?,
    configuration: UpscalerConfiguration?,
    onConfigurationChange: (UpscalerConfiguration?) -> Unit, onConfigure: () -> Unit,
    modifier: Modifier = Modifier, isEnabled: Boolean = true, isStandalone: Boolean = false,
    outputSize: UpscalerImageSize? = null,
    onRevealControls: suspend (isExpanding: () -> Boolean) -> Unit = {},
) {
    if (state == null || !state.isSupported) return
    val isVisible = isStandalone || configuration != null
    val visibility = remember { MutableTransitionState(isVisible) }
    visibility.targetState = isVisible
    var shouldRevealControls by remember { mutableStateOf(false) }
    LaunchedEffect(shouldRevealControls, isVisible) {
        if (shouldRevealControls && isVisible) {
            onRevealControls { !visibility.isIdle || !visibility.currentState }
            shouldRevealControls = false
        }
    }
    val defaultModel = state.model?.takeIf { it.downloaded }
    val source = configuration?.source ?: state.model?.let { UpscalerSource.forModel(it.id) }
    ?: UpscalerSource.availableOn(state.platform).first()
    LaunchedEffect(defaultModel?.id, state.isLoading) {
        if (!state.isLoading && (isStandalone || configuration != null)) {
            onConfigurationChange(defaultModel?.let {
                UpscalerConfiguration(
                    it.id,
                    scaleFactor = configuration?.scaleFactor
                        ?: UpscalerSource.forModel(it.id).nativeScaleFactor
                )
            }
                ?: if (isStandalone) null else UpscalerConfiguration("", source))
        }
    }
    LaunchedEffect(state.isLoading, state.models) {
        if (!state.isLoading) {
            if (isStandalone && configuration == null)
                onConfigurationChange(defaultModel?.let {
                    UpscalerConfiguration(
                        it.id,
                        scaleFactor = configuration?.scaleFactor
                            ?: UpscalerSource.forModel(it.id).nativeScaleFactor
                    )
                })
            else if (configuration != null && state.models.none { it.id == configuration.modelId && it.downloaded }) {
                val replacement = defaultModel?.let {
                    UpscalerConfiguration(it.id, scaleFactor = configuration.scaleFactor)
                }
                onConfigurationChange(
                    replacement ?: if (isStandalone) null else UpscalerConfiguration("", source)
                )
            }
        }
    }
    Column(modifier.fillMaxWidth()) {
        if (!isStandalone) UpscalerEnableCard(
            isChecked = configuration != null, isEnabled = isEnabled,
            onCheckedChange = { shouldUpscale ->
                shouldRevealControls = shouldUpscale
                onConfigurationChange(
                    if (shouldUpscale) UpscalerConfiguration(
                        defaultModel?.id.orEmpty(),
                        source
                    ) else null
                )
            },
        )
        AnimatedVisibility(
            visibleState = visibility,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            Column(
                modifier = Modifier.padding(top = if (isStandalone) 0.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (!isStandalone) UpscalerProviderCard(
                    platform = state.platform, source = source,
                    onConfigure = onConfigure,
                )
                UpscalerParametersForm(
                    configuration = configuration,
                    models = state.models.filter { UpscalerSource.forModel(it.id) == source },
                    onConfigurationChange = onConfigurationChange,
                    isEnabled = isEnabled,
                    isStandalone = isStandalone,
                    outputSize = outputSize,
                )
            }
        }
        state.error?.let {
            Text(
                it,
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
