package com.shifthackz.aisdv1.data.preference.backup

/**
 * Storage binding for one stable portable field.
 *
 * @param key Existing platform preference key.
 * @param type Expected persisted value type.
 * @author Dmitriy Moroz
 */
internal data class PortablePreferenceField(val key: String, val type: PortablePreferenceType)
