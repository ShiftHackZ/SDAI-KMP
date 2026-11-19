package com.shifthackz.aisdv1.domain.usecase.splash


/**
 * Chooses startup routing from onboarding, recovery and saved provider state.
 *
 * @author Dmitriy Moroz
 */
interface SplashNavigationUseCase {
    /**
     * Returns the first route that can safely publish the current persisted configuration.
     *
     * @return Startup destination after pending restore checks.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(): Action

    /**
     * Startup destinations evaluated before normal provider loading.
     *
     * @author Dmitriy Moroz
     */
    enum class Action {
        LAUNCH_ONBOARDING,
        LAUNCH_BACKUP_WELCOME,
        LAUNCH_BACKUP_RESTORE,
        LAUNCH_INITIAL_RESTORE,
        LAUNCH_SERVER_SETUP,
        LAUNCH_HOME;
    }
}
