package com.shifthackz.aisdv1.domain.entity.backup


/**
 * One selectable inventory or inspected archive category.
 * @param category Known section identity.
 * @param itemCount Number of saved entries, not an estimated compressed size.
 * @param isAvailable Whether the current build can import or export this section.
 * @author Dmitriy Moroz
 */
data class BackupEntry(
    val category: BackupCategory,
    val itemCount: Long,
    val isAvailable: Boolean = true,
)
