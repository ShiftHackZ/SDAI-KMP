package com.shifthackz.aisdv1.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Consumed WebSocket fields; unrelated status and preview messages are ignored.
 *
 * @param progress Workflow completion for the generated image.
 * @param image Generated image path or data URL.
 * @param batchIndex Image identity, with negative indices reserved for previews/grids.
 * @param discardIndices Image identities removed by server postprocessing.
 * @param error Server-reported generation failure.
 * @param errorId Machine-readable server error, including invalid_session_id.
 * @param socketIntention Close marks the end of generation and postprocessing.
 * @author Dmitriy Moroz
 */
@Serializable
data class SwarmUiProgressFrame(
    @SerialName("gen_progress") val progress: SwarmUiWorkflowProgress? = null,
    val image: String? = null,
    @SerialName("batch_index") val batchIndex: String? = null,
    @SerialName("discard_indices") val discardIndices: List<Int> = emptyList(),
    val error: String? = null,
    @SerialName("error_id") val errorId: String? = null,
    @SerialName("socket_intention") val socketIntention: String? = null,
)
