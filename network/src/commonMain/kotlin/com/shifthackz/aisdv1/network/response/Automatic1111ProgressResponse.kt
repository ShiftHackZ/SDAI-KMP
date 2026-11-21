package com.shifthackz.aisdv1.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Consumed fields from the public progress endpoint; older servers may omit request correlation.
 * @param progress Job completion fraction including server-side batch iterations.
 * @param currentTask Identity of the active job, absent on unsupported versions.
 * @author Dmitriy Moroz
 */
@Serializable
data class Automatic1111ProgressResponse(
    val progress: Float? = null,
    @SerialName("current_task") val currentTask: String? = null,
)
