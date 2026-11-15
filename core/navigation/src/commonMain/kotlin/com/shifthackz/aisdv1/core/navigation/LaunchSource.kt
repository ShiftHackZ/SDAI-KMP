package com.shifthackz.aisdv1.core.navigation

/**
 * Origin of onboarding or provider setup. Initial launch replaces gating routes; Settings launches retain a return path.
 *
 * @author Dmitriy Moroz
 */
enum class LaunchSource {
    SPLASH,
    SETTINGS;

    /**
     * Legacy integer-key decoding for persisted navigation launch context.
     *
     * @author Dmitriy Moroz
     */
    companion object {
        /**
         * Decodes the existing ordinal key, falling back to initial launch for unknown values.
         *
         * @param key Existing ordinal key supplied by a navigation caller.
         * @author Dmitriy Moroz
         */
        fun fromKey(key: Int) = entries.firstOrNull { it.ordinal == key } ?: SPLASH
    }
}
