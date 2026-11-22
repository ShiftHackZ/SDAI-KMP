package com.shifthackz.aisdv1.domain.entity

/**
 * Validated Civitai account identity
 * credentials never enter presentation state.
 *
 * @param id Civitai account ID used to separate upload history between accounts.
 * @param username Public account name displayed before publishing.
 * @author Dmitriy Moroz
 */
data class CivitaiAccount(val id: Long, val username: String)
