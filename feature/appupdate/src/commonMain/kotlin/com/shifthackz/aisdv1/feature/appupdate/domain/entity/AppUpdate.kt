package com.shifthackz.aisdv1.feature.appupdate.domain.entity

/**
 * An available distribution update. A null URL delegates installation to Google Play.
 *
 * @param version Human-readable release version or nightly build identifier.
 * @param url Trusted release or App Store page; null selects the native Play flow.
 * @author Dmitriy Moroz
 */
data class AppUpdate(val version: String, val url: String?)
