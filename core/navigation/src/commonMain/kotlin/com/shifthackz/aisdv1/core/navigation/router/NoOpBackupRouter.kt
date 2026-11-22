package com.shifthackz.aisdv1.core.navigation.router

/**
 * Inert backup navigation for previews and unbound composition roots.
 *
 * @author Dmitriy Moroz
 */
object NoOpBackupRouter : BackupRouter {
    override fun navigateToHomeScreen() = Unit
    override fun navigateBack() = Unit
    override fun navigateToInitialRestore() = Unit
    override fun navigateToInitialConfiguration() = Unit
}
