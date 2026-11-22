package com.shifthackz.aisdv1.feature.appupdate

import kotlinx.serialization.Serializable

/**
 * Apple's public lookup envelope; an empty list means unavailable in this storefront.
 *
 * @param results Matching store applications.
 * @author Dmitriy Moroz
 */
@Serializable
internal data class AppStoreLookup(val results: List<AppStoreRelease>)
