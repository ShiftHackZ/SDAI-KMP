package com.shifthackz.aisdv1.feature.mnn

/**
 * Terminal Apple result emitted only after native resources return to their owner.
 *
 * @param imageBase64 Complete validated PNG present only on success.
 * @param errorMessage Recoverable failure message, absent on success.
 * @param isCancelled Whether this exact owner was interrupted.
 *
 * @author Dmitriy Moroz
 */
data class MnnAppleResult(
    val imageBase64: String?,
    val errorMessage: String?,
    val isCancelled: Boolean
)
