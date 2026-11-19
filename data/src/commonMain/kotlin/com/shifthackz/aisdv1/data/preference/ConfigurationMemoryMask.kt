package com.shifthackz.aisdv1.data.preference

/**
 * Last trusted values for a complete checked configuration replacement.
 * Null preserves an absent entry so callers retain their own defaults. Never log this object.
 *
 * @param strings Trusted string entries immediately before replacement.
 * @param booleans Trusted boolean entries immediately before replacement.
 * @param candidateStrings Exact string batch admitted for checked retry.
 * @param candidateBooleans Exact boolean batch admitted for checked retry.
 * @param integers Trusted integer values before a checked settings batch.
 * @param candidateIntegers Exact integer replacements retained for retry.
 * @author Dmitriy Moroz
 */
internal class ConfigurationMemoryMask(
    val strings: Map<String, String?>,
    val booleans: Map<String, Boolean?>,
    val candidateStrings: Map<String, String>,
    val candidateBooleans: Map<String, Boolean>,
    val integers: Map<String, Int?> = emptyMap(),
    val candidateIntegers: Map<String, Int> = emptyMap(),
) {
    /**
     * Identifies entries owned by the pending transaction, excluding unrelated settings.
     *
     * @param key Preference key being accessed.
     * @author Dmitriy Moroz
     */
    fun contains(key: String): Boolean =
        strings.containsKey(key) || booleans.containsKey(key) || integers.containsKey(key)

    /**
     * Allows only a identical complete key/value batch to retry while reads are masked.
     *
     * @param strings Proposed string replacements.
     * @param booleans Proposed boolean replacements.
     * @param integers Proposed integer replacements.
     * @author Dmitriy Moroz
     */
    fun matches(
        strings: Map<String, String>,
        booleans: Map<String, Boolean>,
        integers: Map<String, Int> = emptyMap()
    ): Boolean =
        candidateStrings == strings && candidateBooleans == booleans && candidateIntegers == integers
}
