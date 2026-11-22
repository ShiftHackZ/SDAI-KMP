package com.shifthackz.aisdv1.data.civitai

/**
 * Protected storage for one complete Civitai key/account record.
 *
 * @author Dmitriy Moroz
 */
internal interface CivitaiCredentialStore {
    /**
     * Returns the complete encrypted record
     * null means signed out, failures propagate.
     *
     * @author Dmitriy Moroz
     */
    fun read(): String?

    /**
     * Atomically replaces or removes a credential record.
     *
     * @param value Complete encoded record, or null for local logout.
     * @author Dmitriy Moroz
     */
    fun replace(value: String?)
}
