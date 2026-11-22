package com.shifthackz.aisdv1.domain.entity.backup


/**
 * Durable post-onboarding recovery stage. Existing installations default to COMPLETE.
 *
 * @author Dmitriy Moroz
 */
enum class BackupStartupStage { INVITATION, RESTORE, CONFIGURATION, COMPLETE }
