package com.shifthackz.aisdv1.domain.entity

/**
 * Immutable remote ownership token; stop and stream calls always use this captured endpoint.
 * @param url Captured HTTP(S) root or reverse-proxy prefix of the owning instance.
 * @param sessionId Unique operation UUID, shared only by this sequential image batch.
 * @param id Positive Python task identity for task-specific stream and stop calls.
 * @author Dmitriy Moroz
 */
data class EasyDiffusionTask(val url: String, val sessionId: String, val id: Long)
