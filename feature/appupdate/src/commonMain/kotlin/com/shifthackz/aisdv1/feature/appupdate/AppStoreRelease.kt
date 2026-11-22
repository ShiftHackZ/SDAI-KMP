package com.shifthackz.aisdv1.feature.appupdate

import kotlinx.serialization.Serializable

/**
 * Published App Store metadata used for optional update discovery.
 *
 * @param trackId Numeric store identifier, checked before showing a prompt.
 * @param version Public marketing version; build numbers are not exposed by lookup.
 * @author Dmitriy Moroz
 */
@Serializable
internal data class AppStoreRelease(val trackId: Long, val version: String)
