package com.shifthackz.aisdv1.domain.entity.backup


/**
 * Progress emitted after bounded work, with committed restore counts distinguished by stage.
 *
 * @param category Current section, or null while staging/validating.
 * @param completed Processed entry count in the current section.
 * @param total Expected entries in the section.
 * @author Dmitriy Moroz
 */
data class BackupProgress(val category: BackupCategory?, val completed: Long, val total: Long)
