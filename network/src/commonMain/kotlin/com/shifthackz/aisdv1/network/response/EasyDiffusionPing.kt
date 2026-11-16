package com.shifthackz.aisdv1.network.response

import kotlinx.serialization.Serializable

/** Readiness status returned by /ping; unknown operational diagnostics are ignored.
 * @param status Server readiness/liveness state such as Online, LoadingModel, Rendering or Init.
 * @author Dmitriy Moroz
 */
@Serializable
data class EasyDiffusionPing(val status: String)
