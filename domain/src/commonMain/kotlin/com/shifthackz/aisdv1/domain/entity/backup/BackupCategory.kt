package com.shifthackz.aisdv1.domain.entity.backup


/**
 * Selectable portable data sections; wire identifiers remain stable across application versions.
 *
 * @param key Permanent section identifier stored in backup manifests.
 * @author Dmitriy Moroz
 */
enum class BackupCategory(val key: String) {
    PROVIDERS("providers"), GALLERY("gallery"), NETWORK("network"), CIVITAI("civitai"), SETTINGS("settings");
}
