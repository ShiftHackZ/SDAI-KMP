package com.shifthackz.aisdv1.presentation.benchmark

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.extensions.shimmer
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.core.navigation.router.BenchmarkRouter
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkAccelerationStatus
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkAccelerator
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkDeviceInfo
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkPlatform
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkProviderIssue
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkProviderRecommendation
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkResult
import com.shifthackz.aisdv1.domain.entity.benchmark.accelerationCapabilities
import com.shifthackz.aisdv1.presentation.components.model.compactDisplayName
import com.shifthackz.aisdv1.presentation.components.model.isAvailableOn
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

/**
 * Connects device benchmark state and run/share/navigation actions to the remembered benchmark ViewModel.
 *
 * @param modifier Placement of the complete benchmark scaffold.
 * @param router Back-navigation owner; defaults to the application graph binding.
 * @author Dmitriy Moroz
 */
@Composable
fun BenchmarkScreen(
    modifier: Modifier = Modifier,
    router: BenchmarkRouter? = null,
) {
    val koin = remember { getKoin() }
    val resolvedRouter = remember(koin, router) {
        router ?: koin.get<BenchmarkRouter>()
    }
    val viewModel = remember(koin, resolvedRouter) {
        koin.get<BenchmarkViewModel> {
            parametersOf(resolvedRouter)
        }
    }
    MviComponent(viewModel = viewModel) { state, intentHandler ->
        BenchmarkScreenContent(
            modifier = modifier,
            state = state,
            processIntent = intentHandler,
        )
    }
}

/**
 * Presents the measured score and run metadata on the themed primary-container surface.
 * Loading retains the existing score placeholders; an active run adds its progress indicator.
 *
 * @param result Latest completed result, or null before the first successful benchmark.
 * @param isLoading Whether a saved result is still being loaded.
 * @param isRunning Whether a benchmark is currently executing.
 * @param modifier Score-card placement and sizing within the benchmark list.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun ScoreSection(
    result: BenchmarkResult?,
    isLoading: Boolean,
    isRunning: Boolean,
    modifier: Modifier = Modifier,
) {
    PlatformSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Localization.string("benchmark_score"),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    if (isLoading && result == null) {
                        ShimmerLine(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .width(96.dp)
                                .height(36.dp),
                        )
                    } else {
                        Text(
                            text = result?.totalScore?.toString()
                                ?: Localization.string("benchmark_no_results"),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            if (isLoading && result == null) {
                ScoreInfoRowSkeleton()
                ScoreInfoRowSkeleton()
            } else if (result != null) {
                ScoreInfoRow(
                    label = Localization.string("benchmark_estimated_time_short"),
                    value = Localization.string("benchmark_seconds", result.estimatedTimeSeconds),
                )
                ScoreInfoRow(
                    label = Localization.string("benchmark_last_run"),
                    value = formatBenchmarkTimestamp(result.createdAt),
                )
            }
            if (isRunning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ScoreInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            modifier = Modifier.weight(0.42f),
            text = label,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            modifier = Modifier.weight(0.58f),
            text = value,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Shows the device snapshot used for the score, with skeletons before capture and unknown-value fallbacks.
 *
 * @param isLoading Whether hardware capture is still pending.
 * @param deviceInfo Current hardware snapshot or the snapshot from the stored result.
 * @param modifier Placement of the hardware card in the benchmark list.
 * @author Dmitriy Moroz
 */
@Composable
internal fun DeviceSection(
    isLoading: Boolean,
    deviceInfo: BenchmarkDeviceInfo?,
    modifier: Modifier = Modifier,
) {
    BenchmarkSectionCard(
        icon = Icons.Default.Memory,
        title = Localization.string("benchmark_hardware"),
        modifier = modifier,
    ) {
        if (isLoading && deviceInfo == null) {
            TableRowsSkeleton(count = 7)
        } else {
            val info = deviceInfo
            InfoRow(Localization.string("benchmark_device"), info?.deviceName().orUnknown())
            InfoRow(Localization.string("benchmark_os"), info?.osVersion.orUnknown())
            InfoRow(Localization.string("benchmark_cpu"), info?.cpuName.orUnknown())
            InfoRow(
                Localization.string("benchmark_processors"),
                info?.cpuCores?.toString().orUnknown(),
            )
            InfoRow(Localization.string("benchmark_gpu"), info?.gpuName.orUnknown())
            InfoRow(
                Localization.string("benchmark_ram"),
                info?.totalRamMb?.let { Localization.string("benchmark_mb", it) }.orUnknown(),
            )
            InfoRow(
                Localization.string("benchmark_vram"),
                info?.totalVramMb?.let { Localization.string("benchmark_mb", it) }
                    ?: Localization.string("benchmark_unknown"),
            )
        }
    }
}

/**
 * Shows every acceleration path using the existing device capability mapping and status colors.
 *
 * @param isLoading Whether the hardware snapshot is still being captured.
 * @param deviceInfo Snapshot used to derive supported, uncertain or unavailable acceleration paths.
 * @param modifier Placement of the acceleration card in the benchmark list.
 * @author Dmitriy Moroz
 */
@Composable
internal fun CapabilitiesSection(
    isLoading: Boolean,
    deviceInfo: BenchmarkDeviceInfo?,
    modifier: Modifier = Modifier,
) {
    BenchmarkSectionCard(
        icon = Icons.Default.DeveloperBoard,
        title = Localization.string("benchmark_capabilities"),
        modifier = modifier,
    ) {
        if (isLoading && deviceInfo == null) {
            TableRowsSkeleton(count = 6)
        } else {
            val capabilities = deviceInfo?.accelerationCapabilities().orEmpty()
            BenchmarkAccelerator.entries.forEach { accelerator ->
                val capability = capabilities.firstOrNull { it.accelerator == accelerator }
                InfoRowContent(label = accelerator.displayName()) {
                    AccelerationStatusChip(status = capability?.status)
                }
            }
        }
    }
}

/**
 * Shows stored per-provider recommendations or the platform's local-provider placeholders before a run.
 *
 * @param isLoading Whether the stored result is still being read.
 * @param deviceInfo Platform snapshot used when no saved provider recommendations exist.
 * @param recommendations Saved recommendations from the displayed measurement.
 * @param modifier Placement of the recommendation card in the benchmark list.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ProviderRecommendationsSection(
    isLoading: Boolean,
    deviceInfo: BenchmarkDeviceInfo?,
    recommendations: List<BenchmarkProviderRecommendation>,
    modifier: Modifier = Modifier,
) {
    val providers = recommendations.ifEmpty {
        deviceInfo?.localBenchmarkProviders().orEmpty().map { provider ->
            BenchmarkProviderRecommendation(
                provider = provider,
                recommended = false,
                width = 0,
                height = 0,
                samplingSteps = 0,
                cfgScale = 0f,
                batchCount = 1,
                estimatedTimeSeconds = 0,
                backgroundGeneration = false,
                sdxlBackend = SdxlBackend.CPU,
            )
        }
    }
    BenchmarkSectionCard(
        icon = Icons.Default.Tune,
        title = Localization.string("benchmark_recommendations"),
        modifier = modifier,
    ) {
        if (isLoading && recommendations.isEmpty()) {
            ProviderRecommendationsSkeleton()
        } else if (providers.isEmpty()) {
            Text(
                text = Localization.string("benchmark_recommendations_empty"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                providers.forEach { recommendation ->
                    ProviderRecommendationBlock(
                        recommendation = recommendation,
                        platform = deviceInfo?.platform?.toPlatform() ?: Platform.ANDROID,
                        pending = recommendations.isEmpty(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderRecommendationBlock(
    recommendation: BenchmarkProviderRecommendation,
    platform: Platform,
    pending: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        ProviderRecommendationTitle(
            text = Localization.string(
                "benchmark_provider_recommended_settings",
                recommendation.provider.compactDisplayName(platform),
            ),
        )
        when {
            pending -> ProviderRecommendationNote(
                text = Localization.string("benchmark_recommendations_empty"),
            )

            !recommendation.recommended -> {
                InfoRow(
                    Localization.string("benchmark_status"),
                    Localization.string("benchmark_provider_not_recommended"),
                )
                recommendation.issues.ifEmpty {
                    listOf(BenchmarkProviderIssue.LOW_SCORE)
                }.forEach { issue ->
                    ProviderRecommendationNote(text = issue.localizedText())
                }
            }

            else -> {
                InfoRow(
                    Localization.string("benchmark_recommended_size"),
                    "${recommendation.width} x ${recommendation.height}",
                )
                InfoRow(
                    Localization.string("benchmark_recommended_steps"),
                    recommendation.samplingSteps.toString(),
                )
                InfoRow(
                    Localization.string("benchmark_recommended_cfg"),
                    recommendation.cfgScale.toString(),
                )
                InfoRow(
                    Localization.string("benchmark_estimated_time_short"),
                    Localization.string("benchmark_seconds", recommendation.estimatedTimeSeconds),
                )
                InfoRow(
                    Localization.string("benchmark_recommended_background"),
                    if (recommendation.backgroundGeneration) {
                        Localization.string("benchmark_recommended")
                    } else {
                        Localization.string("benchmark_not_recommended")
                    },
                )
                if (recommendation.provider == ServerSource.LOCAL_STABLE_DIFFUSION_CPP) {
                    InfoRow(
                        Localization.string("benchmark_recommended_backend"),
                        recommendation.sdxlBackend.displayName(),
                    )
                }
                recommendation.issues.forEach { issue ->
                    ProviderRecommendationNote(text = issue.localizedText())
                }
            }
        }
    }
}

@Composable
private fun ProviderRecommendationTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Composable
private fun ProviderRecommendationNote(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}


@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    InfoRowContent(
        label = label,
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun InfoRowContent(
    label: String,
    modifier: Modifier = Modifier,
    value: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            modifier = Modifier.weight(0.42f),
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier.weight(0.58f),
        ) {
            value()
        }
    }
}

@Composable
private fun AccelerationStatusChip(
    status: BenchmarkAccelerationStatus?,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(status.containerColor())
            .padding(horizontal = 6.dp, vertical = 2.dp),
        text = status.localizedText(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.W500,
        color = status.contentColor(),
    )
}

@Composable
private fun ScoreInfoRowSkeleton(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShimmerLine(
            modifier = Modifier
                .weight(0.42f)
                .height(16.dp),
        )
        ShimmerLine(
            modifier = Modifier
                .weight(0.58f)
                .height(16.dp),
        )
    }
}

@Composable
private fun TableRowsSkeleton(count: Int) {
    repeat(count) { index ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerLine(
                modifier = Modifier
                    .weight(0.42f)
                    .height(16.dp),
            )
            ShimmerLine(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxWidth(if (index % 2 == 0) 0.86f else 0.68f)
                    .height(16.dp),
            )
        }
    }
}

@Composable
private fun ProviderRecommendationsSkeleton(
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier,
    ) {
        repeat(3) { blockIndex ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerLine(
                    modifier = Modifier
                        .fillMaxWidth(if (blockIndex == 0) 0.62f else 0.74f)
                        .height(18.dp),
                )
                repeat(if (blockIndex == 0) 4 else 2) { index ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ShimmerLine(
                            modifier = Modifier
                                .weight(0.42f)
                                .height(16.dp),
                        )
                        ShimmerLine(
                            modifier = Modifier
                                .weight(0.58f)
                                .fillMaxWidth(if (index % 2 == 0) 0.55f else 0.72f)
                                .height(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShimmerLine(modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .shimmer(),
    )
}

private fun BenchmarkDeviceInfo.deviceName(): String =
    listOf(manufacturer, model)
        .filter(String::isNotBlank)
        .joinToString(" ")
        .ifBlank { Localization.string("benchmark_unknown") }

private fun BenchmarkDeviceInfo.localBenchmarkProviders(): List<ServerSource> =
    platform.toPlatformOrNull()?.let { targetPlatform ->
        ServerSource.entries.filter { source ->
            source.type == ServerSourceType.LOCAL &&
                    source.isAvailableOn(targetPlatform)
        }
    }.orEmpty()

private fun BenchmarkAccelerator.displayName(): String = when (this) {
    BenchmarkAccelerator.VULKAN -> "Vulkan backend"
    BenchmarkAccelerator.BONSAI_VULKAN -> "Bonsai Vulkan compute"
    BenchmarkAccelerator.OPEN_CL -> "OpenCL backend"
    BenchmarkAccelerator.NNAPI -> "NNAPI delegate"
    BenchmarkAccelerator.METAL -> "Metal"
    BenchmarkAccelerator.CORE_ML -> "Core ML"
    BenchmarkAccelerator.NEURAL_ENGINE -> "Neural Engine"
}

private fun BenchmarkAccelerationStatus?.localizedText(): String = when (this) {
    BenchmarkAccelerationStatus.SUPPORTED -> Localization.string("benchmark_backend_supported")
    BenchmarkAccelerationStatus.BACKEND_UNAVAILABLE -> Localization.string("benchmark_backend_unavailable")
    BenchmarkAccelerationStatus.NOT_VALIDATED -> Localization.string("benchmark_backend_not_validated")
    BenchmarkAccelerationStatus.NOT_RECOMMENDED -> Localization.string("benchmark_backend_not_recommended")
    BenchmarkAccelerationStatus.UNAVAILABLE,
    null -> Localization.string("benchmark_unavailable")
}

private fun BenchmarkAccelerationStatus?.containerColor(): Color = when (this) {
    BenchmarkAccelerationStatus.SUPPORTED -> Color(0xFF388E3C)
    BenchmarkAccelerationStatus.NOT_RECOMMENDED -> Color(0xFFFFD54F)
    BenchmarkAccelerationStatus.NOT_VALIDATED -> Color(0xFFF57C00)
    BenchmarkAccelerationStatus.BACKEND_UNAVAILABLE,
    BenchmarkAccelerationStatus.UNAVAILABLE,
    null -> Color(0xFFD32F2F)
}

private fun BenchmarkAccelerationStatus?.contentColor(): Color = when (this) {
    BenchmarkAccelerationStatus.NOT_RECOMMENDED -> Color(0xFF3B2F00)
    else -> Color.White
}

private fun SdxlBackend.displayName(): String = displayName

private fun BenchmarkPlatform.toPlatform(): Platform =
    toPlatformOrNull() ?: Platform.ANDROID

private fun BenchmarkPlatform.toPlatformOrNull(): Platform? = when (this) {
    BenchmarkPlatform.ANDROID -> Platform.ANDROID
    BenchmarkPlatform.IOS -> Platform.IOS
    BenchmarkPlatform.UNKNOWN -> null
}

private fun BenchmarkProviderIssue.localizedText(): String = when (this) {
    BenchmarkProviderIssue.PLATFORM_UNSUPPORTED ->
        Localization.string("benchmark_issue_platform_unsupported")

    BenchmarkProviderIssue.LOW_MEMORY ->
        Localization.string("benchmark_issue_low_memory")

    BenchmarkProviderIssue.LOW_SCORE ->
        Localization.string("benchmark_issue_low_score")

    BenchmarkProviderIssue.ONNX_SLOW_LOW_MEMORY ->
        Localization.string("benchmark_issue_onnx_slow_low_memory")

    BenchmarkProviderIssue.MEDIAPIPE_UNSTABLE_LOW_MEMORY ->
        Localization.string("benchmark_issue_mediapipe_unstable_low_memory")

    BenchmarkProviderIssue.SDXL_TINY_EXPERIMENTAL_ONLY ->
        Localization.string("benchmark_issue_sdxl_tiny_experimental_only")

    BenchmarkProviderIssue.SDXL_BACKEND_NOT_VALIDATED ->
        Localization.string("benchmark_issue_sdxl_backend_not_validated")

    BenchmarkProviderIssue.ACCELERATOR_API_NOT_AVAILABLE ->
        Localization.string("benchmark_issue_accelerator_api_not_available")
}

private fun String?.orUnknown(): String = this ?: Localization.string("benchmark_unknown")
