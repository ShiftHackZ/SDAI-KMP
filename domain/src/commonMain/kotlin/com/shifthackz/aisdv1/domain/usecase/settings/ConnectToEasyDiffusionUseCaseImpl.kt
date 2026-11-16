package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.usecase.connectivity.TestEasyDiffusionConnectivityUseCase

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.isOpenAiCompatibleEndpoint
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.EasyDiffusionRepository
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionTimeoutFailure
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Probes readiness and the full checkpoint catalog before publishing candidate settings.
 * @param repository Strict candidate probe and endpoint-scoped catalog.
 * @param getConfigurationUseCase Snapshot of every provider's independent settings.
 * @param setServerConfigurationUseCase Atomic checked configuration publication and refresh.
 * @param preferences Current connection deadline.
 * @param testConnectivity Candidate readiness policy accepting busy/loading servers.
 * @author Dmitriy Moroz
 */
internal class ConnectToEasyDiffusionUseCaseImpl(
    private val repository: EasyDiffusionRepository,
    private val getConfigurationUseCase: GetConfigurationUseCase,
    private val setServerConfigurationUseCase: SetServerConfigurationUseCase,
    private val preferences: PreferenceManager,
    private val testConnectivity: TestEasyDiffusionConnectivityUseCase,
) : ConnectToEasyDiffusionUseCase {

    /** Validates the candidate instance/catalog, then atomically commits anonymous endpoint/model/source configuration.
     * @param url Captured HTTP(S) instance root or reverse-proxy prefix.
     * @author Dmitriy Moroz */
    override suspend fun invoke(url: String): Result<Unit> = try {
        val candidate = url.trim().trimEnd('/')
        require(isOpenAiCompatibleEndpoint(candidate)) { "Invalid HTTP(S) server URL" }
        val limit =
            preferences.connectionTimeout.seconds.let { if (it > 0) it.seconds else Duration.INFINITE }
        withTimeout(limit) {
            withLocalNetworkPermissionRetry(candidate) {
                testConnectivity(candidate)
                repository.fetchModels(candidate)
            }
            val models = repository.getModels(candidate)
            check(models.isNotEmpty()) { "Install an SD1.5 checkpoint in Easy Diffusion and retry" }
            coroutineContext.ensureActive()
            val previous = getConfigurationUseCase()
            val selected = previous.easyDiffusionModel.takeIf { id -> models.any { it.id == id } }
                ?: models.first().id
            try {
                setServerConfigurationUseCase(
                    previous.copy(
                        source = ServerSource.EASY_DIFFUSION, demoMode = false,
                        easyDiffusionUrl = candidate, easyDiffusionModel = selected,
                        authCredentials = AuthorizationCredentials.None,
                    )
                )
            } catch (failure: Exception) {
                try {
                    withContext(NonCancellable) { setServerConfigurationUseCase(previous) }
                } catch (rollbackFailure: Exception) {
                    failure.addSuppressed(rollbackFailure)
                }
                coroutineContext.ensureActive()
                throw failure
            }
        }
        Result.success(Unit)
    } catch (failure: TimeoutCancellationException) {
        coroutineContext.ensureActive()
        Result.failure(EasyDiffusionTimeoutFailure(failure))
    } catch (failure: CancellationException) {
        throw failure
    } catch (failure: Exception) {
        Result.failure(failure)
    }
}
