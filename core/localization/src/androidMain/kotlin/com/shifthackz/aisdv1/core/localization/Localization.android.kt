package com.shifthackz.aisdv1.core.localization

import java.util.Locale

/**
 * Reads the preferred language reported by the native host.
 *
 * @author Dmitriy Moroz
 */
internal actual fun platformLanguageCode(): String? = Locale.getDefault().language
