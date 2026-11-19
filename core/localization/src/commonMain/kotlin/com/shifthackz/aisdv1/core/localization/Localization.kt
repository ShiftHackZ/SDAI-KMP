package com.shifthackz.aisdv1.core.localization

import androidx.compose.runtime.mutableStateOf

/**
 * A supported application language displayed in the language picker.
 *
 * @param code Normalized language code used to select the bundled catalog.
 * @param name Native language name displayed independently of the selected locale.
 *
 * @author Dmitriy Moroz
 */
data class LocalizationLanguage(
    val code: String,
    val name: String,
)

/**
 * Resolves bundled translations using the selected application language.
 * Language reads participate in Compose snapshot observation, so a locale change invalidates
 * each composition scope that resolved text, including retained tabs and lazy list items.
 *
 * @author Dmitriy Moroz
 */
object Localization {

    /**
     * Fallback catalog used when a language or translation key is unavailable.
     *
     * @author Dmitriy Moroz
     */
    const val DEFAULT_LANGUAGE_CODE = "en"

    private val languageCodeState = mutableStateOf(supportedLanguageCode(platformLanguageCode()))

    /**
     * Supported languages in the catalog order used by the language picker.
     *
     * @author Dmitriy Moroz
     */
    val entries: List<LocalizationLanguage> = localizationLanguages

    /**
     * Returns the active normalized language code and observes it when read during composition.
     *
     * @return Supported language code for the active catalog.
     * @author Dmitriy Moroz
     */
    fun currentLanguageCode(): String = languageCodeState.value

    /**
     * Selects a bundled language and immediately invalidates observing composition scopes.
     * Re-selecting the current language leaves those scopes unchanged.
     *
     * @param code Language code to normalize; null selects the platform language.
     * @author Dmitriy Moroz
     */
    fun setLanguageCode(code: String?) {
        languageCodeState.value = code?.let(::supportedLanguageCode)
            ?: supportedLanguageCode(platformLanguageCode())
    }

    /**
     * Returns the bundled catalog for a language, falling back to English if unsupported.
     *
     * @param languageCode BCP-47 language code handled by the platform layer.
     * @return Translation keys mapped to their localized text.
     * @author Dmitriy Moroz
     */
    fun strings(languageCode: String = currentLanguageCode()): Map<String, String> =
        localizationCatalog.getValue(supportedLanguageCode(languageCode))

    /**
     * Resolves a translation and substitutes sequential or numbered string placeholders.
     * Missing translations fall back to English, then to the key itself.
     *
     * @param key Translation key from the bundled catalog.
     * @param args Values substituted for `%s` or numbered `%1$s` placeholders.
     * @param languageCode BCP-47 language code handled by the platform layer.
     * @author Dmitriy Moroz
     */
    fun string(
        key: String,
        vararg args: Any?,
        languageCode: String = currentLanguageCode(),
    ): String = strings(languageCode)
        .getOrElse(key) { localizationCatalog.getValue(DEFAULT_LANGUAGE_CODE)[key] ?: key }
        .format(args)

    /**
     * Normalizes a locale to a bundled base language, with English as the fallback.
     *
     * @param code Locale code to normalize; null and unsupported codes select English.
     * @return Supported base language code.
     * @author Dmitriy Moroz
     */
    private fun supportedLanguageCode(code: String?): String {
        val normalized = code
            ?.lowercase()
            ?.replace('_', '-')
            ?.substringBefore('-')
            ?.takeIf(String::isNotBlank)

        return normalized
            ?.takeIf(localizationCatalog::containsKey)
            ?: DEFAULT_LANGUAGE_CODE
    }

    /**
     * Substitutes numbered placeholders before consuming sequential string placeholders.
     *
     * @param args Values substituted for `%s` or numbered `%1$s` placeholders.
     * @return Text with supplied placeholder values substituted.
     * @author Dmitriy Moroz
     */
    private fun String.format(args: Array<out Any?>): String {
        var result = this
        args.forEachIndexed { index, arg ->
            result = result.replace("%${index + 1}\$s", arg.toString())
        }
        args.forEach { arg ->
            result = result.replaceFirst("%s", arg.toString())
        }
        return result
    }
}

/**
 * Platform catalog languages and native names generated from the canonical string resources.
 *
 * @author Dmitriy Moroz
 */
internal expect val localizationLanguages: List<LocalizationLanguage>

/**
 * Generated platform translations keyed by normalized language code and translation key.
 *
 * @author Dmitriy Moroz
 */
internal expect val localizationCatalog: Map<String, Map<String, String>>

/**
 * Reads the preferred language supplied by the native host.
 *
 * @return Platform locale code, or null when unavailable.
 * @author Dmitriy Moroz
 */
internal expect fun platformLanguageCode(): String?
