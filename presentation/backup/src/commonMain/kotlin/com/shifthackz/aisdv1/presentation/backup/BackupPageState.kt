package com.shifthackz.aisdv1.presentation.backup

import com.shifthackz.aisdv1.domain.entity.backup.BackupInspection
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection

/**
 * Independent tab inventory and selection; switching tabs retains both pages.
 *
 * @param inspection Available data or validated archive summary.
 * @param selection Explicit category and provider choices.
 * @param areProvidersExpanded Whether nested provider choices are visible.
 * @author Dmitriy Moroz
 */
data class BackupPageState(
    val inspection: BackupInspection? = null,
    val selection: BackupSelection = BackupSelection(),
    val areProvidersExpanded: Boolean = true,
)
