package com.shifthackz.aisdv1.feature.appupdate

import com.shifthackz.aisdv1.feature.appupdate.domain.AppUpdateGateway
import com.shifthackz.aisdv1.feature.appupdate.domain.CheckAppUpdateUseCase
import com.shifthackz.aisdv1.feature.appupdate.domain.entity.AppUpdate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * Bounds optional launch discovery to ten seconds and leaves startup usable on service failures.
 *
 * @param gateway Distribution-specific discovery with strict failure propagation.
 * @author Dmitriy Moroz
 */
internal class CheckAppUpdateUseCaseImpl(private val gateway: AppUpdateGateway) :
    CheckAppUpdateUseCase {
    override suspend fun invoke(): AppUpdate? = try {
        withTimeoutOrNull(10_000.milliseconds) { gateway.check() }
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        null
    }
}
