package com.shifthackz.aisdv1.feature.appupdate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Public GitHub release fields consumed by update discovery.
 *
 * @param tag Published Git tag.
 * @param isDraft Whether this release is unpublished.
 * @param isPrerelease Whether this is outside the stable channel.
 * @param body CI release notes containing the nightly version and monotonically increasing code.
 * @param assets Attached distributable files.
 * @author Dmitriy Moroz
 */
@Serializable
internal data class GitHubRelease(
    @SerialName("tag_name") val tag: String,
    @SerialName("draft") val isDraft: Boolean,
    @SerialName("prerelease") val isPrerelease: Boolean,
    val body: String? = null,
    val assets: List<GitHubAsset> = emptyList(),
)
