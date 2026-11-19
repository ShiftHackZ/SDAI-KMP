package com.shifthackz.aisdv1.domain.entity.backup


/**
 * Explicit user choices shared by create and restore screens.
 * @param categories Selected available data categories.
 * @param providers Selected provider wire identifiers beneath the providers category.
 * @author Dmitriy Moroz
 */
data class BackupSelection(
    val categories: Set<BackupCategory> = emptySet(),
    val providers: Set<String> = emptySet(),
)
