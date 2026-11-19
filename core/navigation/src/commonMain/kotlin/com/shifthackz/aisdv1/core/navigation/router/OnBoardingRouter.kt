package com.shifthackz.aisdv1.core.navigation.router


/**
 * Navigation actions consumed by the on boarding screen. The host owns route and back-stack mutation.
 *
 * @author Dmitriy Moroz
 */
interface OnBoardingRouter {
    /**
     * Replaces completed onboarding with the first-launch recovery invitation.
     * @author Dmitriy Moroz
     */
    fun navigateToBackupWelcome()

    /**
     * Closes the drawer first, otherwise returns to the previous route or the generation-home fallback.
     *
     * @author Dmitriy Moroz
     */
    fun navigateBack()


}
