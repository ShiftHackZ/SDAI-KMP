package com.shifthackz.aisdv1.presentation.benchmark.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkPlatform
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkProviderIssue
import com.shifthackz.aisdv1.presentation.benchmark.BenchmarkState

/**
 * Supplies screen states and explicit lower-section snapshots so all four sections can be inspected.
 * Capability badges use the production mapping; recommendation issues use fixed illustrative data.
 *
 * @author Dmitriy Moroz
 */
internal class BenchmarkPreviewParameterProvider : PreviewParameterProvider<BenchmarkPreviewCase> {
    private val ready = BenchmarkPreviewFixtures.ready
    private val result = BenchmarkPreviewFixtures.result
    private val android = BenchmarkPreviewFixtures.android
    private val noResult = ready.copy(latestResult = null)
    private val cases = listOf(
        BenchmarkPreviewCase("Initial loading", BenchmarkState()),
        BenchmarkPreviewCase("Hardware loading", BenchmarkState(), sectionIndex = 1),
        BenchmarkPreviewCase("Capabilities loading", BenchmarkState(), sectionIndex = 2),
        BenchmarkPreviewCase("Recommendations loading", BenchmarkState(), sectionIndex = 3),
        BenchmarkPreviewCase("No previous result", noResult),
        BenchmarkPreviewCase(
            "Missing hardware",
            noResult.copy(deviceInfo = null),
            sectionIndex = 1
        ),
        BenchmarkPreviewCase("Pending local providers", noResult, sectionIndex = 3),
        BenchmarkPreviewCase(
            "No known platform providers",
            noResult.copy(deviceInfo = null),
            sectionIndex = 3
        ),
        BenchmarkPreviewCase("Score and last run", ready),
        BenchmarkPreviewCase("Hardware details", ready, sectionIndex = 1),
        BenchmarkPreviewCase("Android capabilities", ready, sectionIndex = 2),
        BenchmarkPreviewCase(
            "Unavailable acceleration",
            noResult.copy(deviceInfo = android.copy(accelerators = emptyList())),
            sectionIndex = 2,
        ),
        BenchmarkPreviewCase(
            "Low-memory backend restrictions",
            noResult.copy(deviceInfo = BenchmarkPreviewFixtures.lowMemory),
            sectionIndex = 2,
        ),
        BenchmarkPreviewCase(
            "iOS capabilities",
            noResult.copy(deviceInfo = BenchmarkPreviewFixtures.ios),
            sectionIndex = 2,
        ),
        BenchmarkPreviewCase(
            "Unknown platform",
            noResult.copy(deviceInfo = android.copy(platform = BenchmarkPlatform.UNKNOWN)),
            sectionIndex = 2,
        ),
        BenchmarkPreviewCase("Provider recommendations", ready, sectionIndex = 3),
        BenchmarkPreviewCase(
            "Recommended with caution notes",
            ready.copy(
                latestResult = result.copy(
                    providerRecommendations = listOf(
                        BenchmarkPreviewFixtures.provider.copy(
                            provider = ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
                            issues = listOf(
                                BenchmarkProviderIssue.SDXL_TINY_EXPERIMENTAL_ONLY,
                                BenchmarkProviderIssue.SDXL_BACKEND_NOT_VALIDATED,
                            ),
                        )
                    ),
                )
            ),
            sectionIndex = 3,
        ),
        BenchmarkPreviewCase(
            "Stored hardware fallback",
            ready.copy(deviceInfo = null, loadingDevice = true),
            sectionIndex = 1
        ),
        BenchmarkPreviewCase(
            "Reading result with captured hardware",
            noResult.copy(loadingResult = true)
        ),
        BenchmarkPreviewCase("Running first benchmark", noResult.copy(running = true)),
        BenchmarkPreviewCase("Running again with stored result", ready.copy(running = true)),
        BenchmarkPreviewCase(
            "Initial failure",
            noResult.copy(error = "Unable to capture device information")
        ),
        BenchmarkPreviewCase(
            "Failure with saved result",
            ready.copy(error = "The benchmark could not complete. Try again.")
        ),
        BenchmarkPreviewCase(
            "Long failure message",
            ready.copy(
                error = "The benchmark could not complete because available memory changed during the run. The previous result remains available. Close other applications and try again.",
            ),
        ),
        BenchmarkPreviewCase(
            "Long hardware fields and dedicated VRAM",
            noResult.copy(
                deviceInfo = android.copy(
                    manufacturer = "Sample manufacturer",
                    model = "Extended device model description",
                    cpuName = "Sample heterogeneous processor with performance and efficiency cores",
                    gpuName = "Sample graphics adapter with a long driver-reported name",
                    totalVramMb = 4096,
                    availableVramMb = 3072,
                )
            ),
            sectionIndex = 1,
        ),
        BenchmarkPreviewCase(
            "iOS pending providers",
            noResult.copy(deviceInfo = BenchmarkPreviewFixtures.ios),
            sectionIndex = 3,
        ),
        BenchmarkPreviewCase(
            "iOS saved recommendation",
            ready.copy(
                deviceInfo = BenchmarkPreviewFixtures.ios,
                latestResult = result.copy(
                    deviceInfo = BenchmarkPreviewFixtures.ios,
                    providerRecommendations = listOf(BenchmarkPreviewFixtures.provider.copy(provider = ServerSource.LOCAL_APPLE_CORE_ML)),
                ),
            ),
            sectionIndex = 3,
        ),
        BenchmarkPreviewCase(
            "Not recommended without explicit issue",
            ready.copy(
                latestResult = result.copy(
                    providerRecommendations = listOf(
                        BenchmarkPreviewFixtures.provider.copy(
                            recommended = false
                        )
                    ),
                )
            ),
            sectionIndex = 3,
        ),
    ) + BenchmarkProviderIssue.entries.map { issue ->
        BenchmarkPreviewCase(
            "Recommendation issue · $issue",
            ready.copy(
                latestResult = result.copy(
                    providerRecommendations = listOf(
                        BenchmarkPreviewFixtures.provider.copy(
                            recommended = false, issues = listOf(issue),
                        )
                    ),
                )
            ),
            sectionIndex = 3,
        )
    } + SdxlBackend.entries.map { backend ->
        BenchmarkPreviewCase(
            "SDCPP backend · $backend",
            ready.copy(
                latestResult = result.copy(
                    providerRecommendations = listOf(
                        BenchmarkPreviewFixtures.provider.copy(
                            provider = ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
                            sdxlBackend = backend,
                            backgroundGeneration = backend != SdxlBackend.CPU,
                        )
                    ),
                )
            ),
            sectionIndex = 3,
        )
    }

    /**
     * Fixed score, hardware, acceleration and recommendation snapshots for both palettes.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<BenchmarkPreviewCase> = cases.asSequence()

    /**
     * Identifies the visible section and state in the preview list.
     *
     * @param index Position in the ordered scenario list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
