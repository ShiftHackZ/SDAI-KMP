package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.caching.DataPreLoaderUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestConnectivityUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Validates and caches a candidate connection within the selected connection timeout.
 * Restores the previous configuration when setup fails.
 *
 * @param getConfigurationUseCase Reads the current configuration for rollback.
 * @param setServerConfigurationUseCase Publishes candidate settings and restores failed attempts.
 * @param preferenceManager Supplies the connection deadline, independently from generation limits.
 * @param testConnectivityUseCase Tests reachability before metadata preload.
 * @param dataPreLoaderUseCase Caches metadata required by generation.
 *
 * @author Dmitriy Moroz
 */
internal class DefaultConnectToA1111UseCaseImpl(
    /**
     * Exposes the `getConfigurationUseCase` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    private val getConfigurationUseCase: GetConfigurationUseCase,
    /**
     * Exposes the `setServerConfigurationUseCase` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    private val setServerConfigurationUseCase: SetServerConfigurationUseCase,
    /**
     * Exposes the `testConnectivityUseCase` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    private val testConnectivityUseCase: TestConnectivityUseCase,
    /**
     * Exposes the `dataPreLoaderUseCase` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    private val dataPreLoaderUseCase: DataPreLoaderUseCase,
    private val preferenceManager: PreferenceManager,
) : ConnectToA1111UseCase {

    /**
     * Validates the candidate within the selected setup deadline and restores settings on failure.
     *
     * @param url remote URL used by the operation.
     * @param isDemo is demo value consumed by the API.
     * @param credentials credentials value consumed by the API.
     * @return Result produced by `invoke`.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(
        url: String,
        isDemo: Boolean,
        credentials: AuthorizationCredentials,
    ): Result<Unit> {
        var configuration: Configuration? = null
        return try {
            withTimeout(setupTimeout()) {
                val originalConfiguration = getConfigurationUseCase()
                configuration = originalConfiguration
                val newConfiguration = originalConfiguration.copy(
                    source = ServerSource.AUTOMATIC1111,
                    serverUrl = url,
                    demoMode = isDemo,
                    authCredentials = credentials,
                )
                setServerConfigurationUseCase(newConfiguration)
                withLocalNetworkPermissionRetry(url) {
                    testConnectivityUseCase(url)
                }
                delay(PRELOAD_DELAY_MILLIS)
                retryRemoteDelayed(PRELOAD_ATTEMPTS, PRELOAD_RETRY_DELAY_MILLIS) {
                    dataPreLoaderUseCase()
                }
            }
            Result.success(Unit)
        } catch (t: Throwable) {
            restoreRemoteConfigurationAndFail(configuration, setServerConfigurationUseCase, t)
        }
    }

    /**
     * Snapshots the current setup limit; zero keeps the operation cancellable without a deadline.
     *
     * @author Dmitriy Moroz
     */
    private fun setupTimeout(): Duration = preferenceManager.connectionTimeout.seconds.let {
        if (it > 0) it.seconds else Duration.INFINITE
    }

    /**
     * Provides the `companion object` singleton used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    private companion object {
        /**
         * Exposes the `PRELOAD_DELAY_MILLIS` value used by the SDAI domain layer.
         *
         * @author Dmitriy Moroz
         */
        const val PRELOAD_DELAY_MILLIS = 5_000L
        /**
         * Exposes the `PRELOAD_RETRY_DELAY_MILLIS` value used by the SDAI domain layer.
         *
         * @author Dmitriy Moroz
         */
        const val PRELOAD_RETRY_DELAY_MILLIS = 1_000L
        /**
         * Exposes the `PRELOAD_ATTEMPTS` value used by the SDAI domain layer.
         *
         * @author Dmitriy Moroz
         */
        const val PRELOAD_ATTEMPTS = 3
    }
}
