package com.shifthackz.aisdv1.feature.appupdate

import kotlinx.serialization.Serializable

/**
 * A release attachment used to avoid offering an incomplete nightly publication.
 *
 * @param name Exact attached filename.
 * @author Dmitriy Moroz
 */
@Serializable
internal data class GitHubAsset(val name: String)
