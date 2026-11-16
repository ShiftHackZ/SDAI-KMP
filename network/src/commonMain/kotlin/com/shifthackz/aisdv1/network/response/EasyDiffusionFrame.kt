package com.shifthackz.aisdv1.network.response

import com.shifthackz.aisdv1.network.model.EasyDiffusionTaskMetadata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Progress or terminal render object; preview outputs without succeeded status are never results.
 * @param status Terminal result status; null denotes a progress object.
 * @param step Current sampling step, or zero while queued/loading.
 * @param totalSteps Sampling total; nonpositive totals produce indeterminate progress.
 * @param output Terminal images; consumed only with a succeeded status.
 * @param detail Remote failure detail marker; never presented as a successful result.
 * @param taskData Optional terminal task metadata used to retain the reported actual checkpoint.
 * @author Dmitriy Moroz
 */
@Serializable
data class EasyDiffusionFrame(
    val status: String? = null,
    val step: Int = 0,
    @SerialName("total_steps") val totalSteps: Int = 0,
    val output: List<EasyDiffusionImage> = emptyList(),
    val detail: String? = null,
    @SerialName("task_data") val taskData: EasyDiffusionTaskMetadata? = null,
)
