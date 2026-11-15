package com.shifthackz.aisdv1.data.preference

/**
 * Last trusted values for a complete checked configuration replacement.
 * Null preserves an absent entry so callers retain their own defaults. Never log this object.
 *
 * @param strings Trusted string entries immediately before replacement.
 * @param booleans Trusted boolean entries immediately before replacement.
 * @param candidateStrings Exact string batch admitted for checked retry.
 * @param candidateBooleans Exact boolean batch admitted for checked retry.
 * @author Dmitriy Moroz
 */
internal class ConfigurationMemoryMask(
    val strings: Map<String, String?>,
    val booleans: Map<String, Boolean?>,
    val candidateStrings: Map<String, String>,
    val candidateBooleans: Map<String, Boolean>,
) {
    /**
     * Identifies entries owned by the pending transaction, excluding unrelated settings.
     *
     * @param key Preference key being accessed.
     * @author Dmitriy Moroz
     */
    fun contains(key: String): Boolean = strings.containsKey(key) || booleans.containsKey(key)

    /**
     * Allows only a identical complete key/value batch to retry while reads are masked.
     *
     * @param strings Proposed string replacements.
     * @param booleans Proposed boolean replacements.
     * @author Dmitriy Moroz
     */
    fun matches(strings: Map<String, String>, booleans: Map<String, Boolean>): Boolean =
        candidateStrings == strings && candidateBooleans == booleans
}
