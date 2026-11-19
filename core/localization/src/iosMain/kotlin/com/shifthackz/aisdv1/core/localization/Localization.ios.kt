package com.shifthackz.aisdv1.core.localization

import platform.Foundation.NSUserDefaults

/**
 * Reads the preferred language reported by the native host.
 *
 * @return Native language code, or null when the host has no preferred language.
 * @author Dmitriy Moroz
 */
internal actual fun platformLanguageCode(): String? =
    NSUserDefaults.standardUserDefaults
        .stringArrayForKey("AppleLanguages")
        ?.firstOrNull() as? String
