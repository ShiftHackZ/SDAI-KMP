package com.shifthackz.aisdv1.network.api.civitai.model

import kotlinx.serialization.Serializable

/**
 * Validated identity fields consumed from user.getSelfStatus.
 *
 * @param id Civitai account identity.
 * @param username Public account name.
 * @author Dmitriy Moroz
 */
@Serializable
data class CivitaiAccountResponse(
    val id: Long,
    val username: String,
)
