package com.shifthackz.aisdv1.domain.entity.backup

/**
 * Typed portable preference primitives. Missing entries retain destination values; never log these objects.
 *
 * @author Dmitriy Moroz
 */
sealed interface BackupValue {
    /**
     * Preserves the presence of an unsupported encoded value without retaining its payload.
     * Compatibility filtering can then count the skipped field instead of silently dropping its ID.
     *
     * @author Dmitriy Moroz
     */
    data object Unsupported : BackupValue

    /**
     * Portable string, including an explicitly saved empty value.
     *
     * @param value Exact field text, which may contain an API key or password.
     * @author Dmitriy Moroz
     */
    data class Text(val value: String) : BackupValue

    /**
     * Portable boolean whose false value remains distinguishable from a missing field.
     *
     * @param isTrue Exact persisted boolean value.
     * @author Dmitriy Moroz
     */
    data class Flag(val isTrue: Boolean) : BackupValue

    /**
     * Portable integral setting such as sampling steps or timeout seconds.
     *
     * @param value Exact stored integer; field-specific validation occurs during inspection.
     * @author Dmitriy Moroz
     */
    data class Integer(val value: Int) : BackupValue
}
