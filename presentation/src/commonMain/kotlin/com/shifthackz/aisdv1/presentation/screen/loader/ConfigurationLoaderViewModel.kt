package com.shifthackz.aisdv1.presentation.screen.loader

import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.core.mvi.EmptyIntent
import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.usecase.caching.DataPreLoaderUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.core.navigation.router.ConfigurationLoaderRouter
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

/**
 * Loads only metadata required by the selected provider before navigating home; unsupported preload paths never invoke AUTOMATIC1111 metadata.
 *
 * @param diagnostics Application UI diagnostics sink used by the normal view-model lifecycle.
 * @param dispatchersProvider Application dispatchers for background I/O and immediate UI state updates.
 * @param dataPreLoaderUseCase Existing AUTOMATIC1111/Swarm metadata preload; bypassed for independent providers.
 * @param getConfigurationUseCase Reads every provider’s independent saved settings and the selected source revision.
 * @param router Navigation destination owner after configuration loading.
 * @param onError Reports loading failures through the application error sink.
 *
 * @author Dmitriy Moroz
 */
class ConfigurationLoaderViewModel(
    diagnostics: UiDiagnostics,

    private val dispatchersProvider: DispatchersProvider,

    private val dataPreLoaderUseCase: DataPreLoaderUseCase,

    private val getConfigurationUseCase: GetConfigurationUseCase,

    private val router: ConfigurationLoaderRouter,

    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<ConfigurationLoaderState, EmptyIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.CONFIGURATION_LOADER.asDiagnosticScreen(),
    initialState = ConfigurationLoaderState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        launch(dispatchersProvider.io) {
            withContext(dispatchersProvider.immediate) {
                emitState(ConfigurationLoaderState(ConfigurationLoaderState.Status.Fetching))
            }

            runCatching {
                val configuration = getConfigurationUseCase()
                if (configuration.requiresRemotePreload()) {
                    withTimeout(STARTUP_PRELOAD_TIMEOUT_MILLIS.milliseconds) {
                        dataPreLoaderUseCase()
                    }
                }
                withContext(dispatchersProvider.immediate) {
                    emitState(ConfigurationLoaderState(ConfigurationLoaderState.Status.Launching))
                    router.navigateToHomeScreen()
                }
            }.onFailure { t ->
                withContext(dispatchersProvider.immediate) {
                    emitState(ConfigurationLoaderState(ConfigurationLoaderState.Status.Failed))
                    router.navigateToHomeScreen()
                }
                onError(t)
            }
        }
    }

    /**
     * Ignores empty intents because initialization alone drives preload and home navigation.
     *
     * @param intent Empty event accepted by the shared MVI contract; it does not restart loading.
     *
     * @author Dmitriy Moroz
     */

    override fun processIntent(intent: EmptyIntent) = Unit
}

/**
 * Maximum time startup waits for legacy remote metadata before allowing home navigation.
 *
 * @author Dmitriy Moroz
 */
private const val STARTUP_PRELOAD_TIMEOUT_MILLIS = 3_000L

/**
 * Restricts legacy metadata preloading to configured AUTOMATIC1111 and SwarmUI instances.
 *
 * @author Dmitriy Moroz
 */
private fun Configuration.requiresRemotePreload(): Boolean = when (source) {
    ServerSource.AUTOMATIC1111 -> serverUrl.isMobileRemoteEndpoint()
    ServerSource.SWARM_UI -> swarmUiUrl.isMobileRemoteEndpoint()
    ServerSource.EASY_DIFFUSION,
    ServerSource.HORDE,
    ServerSource.HUGGING_FACE,
    ServerSource.OPEN_AI,
    ServerSource.OPEN_AI_COMPATIBLE,
    ServerSource.OPEN_AI_OAUTH,
    ServerSource.STABILITY_AI,
    ServerSource.FAL_AI,
    ServerSource.ARLI_AI,
    ServerSource.LOCAL_MNN,
    ServerSource.LOCAL_MICROSOFT_ONNX,
    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
    ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
    ServerSource.LOCAL_APPLE_CORE_ML,
    ServerSource.LOCAL_APPLE_BONSAI,
        -> false
}

/**
 * Checks that a saved endpoint is nonempty and does not point at the mobile device’s loopback interface.
 *
 * @author Dmitriy Moroz
 */
private fun String.isMobileRemoteEndpoint(): Boolean {
    val value = trim().lowercase()
    return value.isNotBlank() &&
            !value.startsWith("http://localhost") &&
            !value.startsWith("https://localhost") &&
            !value.startsWith("http://127.0.0.1") &&
            !value.startsWith("https://127.0.0.1") &&
            !value.startsWith("http://[::1]") &&
            !value.startsWith("https://[::1]")
}
