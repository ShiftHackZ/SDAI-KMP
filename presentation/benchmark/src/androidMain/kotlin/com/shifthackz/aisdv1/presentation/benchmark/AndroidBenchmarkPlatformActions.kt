package com.shifthackz.aisdv1.presentation.benchmark

import android.content.Context
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.sharing.shareText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

/**
 * Hands formatted benchmark results to Android text sharing on the immediate dispatcher.
 * Cancellation propagates; platform-launch failures become public action failures.
 *
 * @param context Launches Android native text sharing.
 * @param dispatchersProvider Supplies the immediate dispatcher for the native UI handoff.
 *
 * @author Dmitriy Moroz
 */
internal class AndroidBenchmarkPlatformActions(
    private val context: Context,
    private val dispatchersProvider: DispatchersProvider,
) : BenchmarkPlatformActions {

    override suspend fun shareText(text: String): BenchmarkActionResult =
        try {
            withContext(dispatchersProvider.immediate) {
                context.shareText(text)
            }
            BenchmarkActionResult.Done
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            BenchmarkActionResult.Failed(t.message ?: "Unable to share benchmark result")
        }
}
