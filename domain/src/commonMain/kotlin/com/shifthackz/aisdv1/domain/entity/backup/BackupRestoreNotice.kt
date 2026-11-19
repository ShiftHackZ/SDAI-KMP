package com.shifthackz.aisdv1.domain.entity.backup


/**
 * Typed reasons for deliberately omitted restore data; no credential values are retained.
 * @author Dmitriy Moroz
 */
sealed interface BackupRestoreNotice {
    /**
     * Shared Basic Auth stayed unchanged to protect configured providers outside the restore selection.
     * @param providers Unselected configured provider IDs whose credentials were preserved.
     * @author Dmitriy Moroz
     */
    data class SharedCredentialsRetained(val providers: Set<String>) : BackupRestoreNotice
}
