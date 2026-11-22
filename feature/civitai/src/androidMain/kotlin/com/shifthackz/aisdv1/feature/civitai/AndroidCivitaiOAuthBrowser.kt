package com.shifthackz.aisdv1.feature.civitai

import android.content.Context
import android.content.Intent
import com.shifthackz.aisdv1.domain.entity.CivitaiOAuthCode
import com.shifthackz.aisdv1.domain.entity.CivitaiAuthorizationCancelled
import com.shifthackz.aisdv1.domain.gateway.CivitaiOAuthBrowser
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * Owns one cancellable native authorization request across the temporary browser Activity.
 *
 * @param context Application context used to open the isolated authorization Activity.
 * @author Dmitriy Moroz
 */
internal class AndroidCivitaiOAuthBrowser(private val context: Context) : CivitaiOAuthBrowser {
    private val mutex = Mutex()
    private var pending: CompletableDeferred<CivitaiOAuthCode>? = null

    /**
     * Opens one PKCE browser request and waits up to ten minutes for its verified result.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun authorize(): CivitaiOAuthCode = mutex.withLock {
        withTimeout(600_000) {
            withContext(Dispatchers.Main.immediate) {
                val result = CompletableDeferred<CivitaiOAuthCode>()
                pending = result
                try {
                    context.startActivity(
                        Intent(context, CivitaiOAuthActivity::class.java).addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    )
                    result.await()
                } finally {
                    pending = null
                }
            }
        }
    }

    /**
     * Completes the current native request
     * late callbacks after cancellation are ignored.
     *
     * @param proof Verified authorization code and original PKCE verifier.
     * @param failure Native cancellation or protocol failure, without response data.
     * @author Dmitriy Moroz
     */
    fun complete(proof: CivitaiOAuthCode?, failure: Throwable?) {
        val result = pending ?: return
        if (proof != null) result.complete(proof) else result.completeExceptionally(
            failure ?: CivitaiAuthorizationCancelled()
        )
    }
}
