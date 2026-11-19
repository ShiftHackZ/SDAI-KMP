package com.shifthackz.aisdv1.data.preference

/**
 * Shared typed preference owner for ordinary settings and checked complete configuration writes.
 * Pending configuration keys retain their last trusted read view; unrelated settings remain usable.
 *
 * @author Dmitriy Moroz
 */
internal interface KeyValueStore {
    /** Retries only an existing quarantined typed batch; false means no write was pending. @author Dmitriy Moroz */
    fun retryPendingWrite(): Boolean

    /**
     * Distinguishes explicitly saved defaults from missing portable fields.
     * @param key Existing preference key.
     * @author Dmitriy Moroz
     */
    fun contains(key: String): Boolean

    /**
     * Checks one atomic typed settings batch before acknowledging restore.
     * @param strings String replacements.
     * @param booleans Boolean replacements.
     * @param integers Integer replacements.
     * @author Dmitriy Moroz
     */
    fun putSettingsDurably(
        strings: Map<String, String>,
        booleans: Map<String, Boolean>,
        integers: Map<String, Int>
    )

    /**
     * Reads a trusted string, using the caller fallback when its prior entry was absent.
     *
     * @param key Existing preference key.
     * @param default Fallback retained while a replacement remains unconfirmed.
     * @author Dmitriy Moroz
     */
    fun getString(key: String, default: String = ""): String

    /**
     * Writes an ordinary setting; platform owners reject overlap with a pending configuration.
     *
     * @param key Existing preference key.
     * @param value New ordinary string value.
     * @author Dmitriy Moroz
     */
    fun putString(key: String, value: String)

    /**
     * Requests checked persistence for an identity value before reporting success to its producer.
     * Platform owners apply the same trusted-memory/exact-retry rules as complete batches.
     *
     * @param key Existing identity preference key.
     * @param value Complete encoded source/revision value.
     * @author Dmitriy Moroz
     */
    fun putStringDurably(key: String, value: String) = putString(key, value)

    /**
     * Checks a complete configuration batch without publishing unconfirmed platform memory.
     * All readers retain prior values/defaults on failure until an identical full retry succeeds.
     * Ordinary writes to these pending keys fail explicitly; unrelated settings remain usable.
     * Platform implementations release their memory lock before disk persistence.
     *
     * @param strings Complete existing string preference keys and candidate values.
     * @param booleans Complete existing boolean preference keys and candidate values.
     * @author Dmitriy Moroz
     */
    fun putConfigurationDurably(strings: Map<String, String>, booleans: Map<String, Boolean>)

    /**
     * Reads a trusted boolean, preserving defaults for previously absent configuration entries.
     *
     * @param key Existing boolean preference key.
     * @param default Fallback for an absent trusted entry.
     * @author Dmitriy Moroz
     */
    fun getBoolean(key: String, default: Boolean = false): Boolean

    /**
     * Writes an ordinary boolean, rejecting conflicts with pending configuration ownership.
     *
     * @param key Existing preference key.
     * @param value New ordinary boolean value.
     * @author Dmitriy Moroz
     */
    fun putBoolean(key: String, value: Boolean)

    /**
     * Reads an ordinary integer; complete configuration batches have no integer entries.
     *
     * @param key Existing integer preference key.
     * @param default Fallback for an absent entry.
     * @author Dmitriy Moroz
     */
    fun getInt(key: String, default: Int = 0): Int

    /**
     * Writes an ordinary integer, rejecting accidental overlap with pending configuration keys.
     *
     * @param key Existing integer preference key.
     * @param value New ordinary integer value.
     * @author Dmitriy Moroz
     */
    fun putInt(key: String, value: Int)
}

/**
 * Creates the SDAI value produced by `createKeyValueStore`.
 *
 * @param name name value consumed by the API.
 * @return Result produced by `createKeyValueStore`.
 * @author Dmitriy Moroz
 */
internal expect fun createKeyValueStore(name: String): KeyValueStore
