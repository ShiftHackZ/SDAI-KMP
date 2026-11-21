package com.shifthackz.aisdv1.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Server workflow completion; node-local current_percent is deliberately not treated as total progress.
 * @param overallPercentage Fraction of workflow execution reported by the active backend.
 * @author Dmitriy Moroz
 */
@Serializable
data class SwarmUiWorkflowProgress(
    @SerialName("overall_percent") val overallPercentage: Float? = null,
)
