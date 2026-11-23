package com.shifthackz.aisdv1.presentation.backup.preview

import com.shifthackz.aisdv1.presentation.backup.BackupState

/**
 * Names one nonsecret backup snapshot and its initial page scroll position.
 *
 * @param name Label displayed in the Android preview list.
 * @param state Inventory, selection and operation feedback rendered by the screen.
 * @param scrollOffset Initial vertical offset; a large value is clamped to the end of the page.
 * @author Dmitriy Moroz
 */
internal data class BackupPreviewCase(
    val name: String,
    val state: BackupState,
    val scrollOffset: Int = 0,
)
