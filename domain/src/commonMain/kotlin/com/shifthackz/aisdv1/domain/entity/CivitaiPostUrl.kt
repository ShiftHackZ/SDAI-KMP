package com.shifthackz.aisdv1.domain.entity

/**
 * Admits only canonical public Civitai post links, including metadata imported from a backup.
 * Credentials, alternate hosts and non-web URI schemes must never become gallery link actions.
 *
 * @return Whether this value identifies a public Civitai post over HTTPS.
 * @author Dmitriy Moroz
 */
fun String.isCivitaiPostUrl(): Boolean = matches(Regex("https://civitai\\.com/posts/[1-9][0-9]*"))
