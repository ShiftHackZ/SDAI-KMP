package com.shifthackz.aisdv1.presentation.components.core

import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.core.notification.PushNotificationManager
import com.shifthackz.aisdv1.core.localization.R as LocalizationR

/**
 * Enables Android background-generation controls and emits localized completion/failure notifications.
 *
 * @param notificationManager Posts immediate foreground-generation outcome notifications through the platform integration.
 *
 * @author Dmitriy Moroz
 */
class AndroidGenerationPlatformServices(

    private val notificationManager: PushNotificationManager,
) : GenerationPlatformServices {

    /**
     * Android exposes generation scheduling through the configured background-work implementation.
     *
     * @author Dmitriy Moroz
     */
    override val supportsBackgroundGeneration: Boolean = true

    /**
     * Posts the localized generation-completed notification.
     *
     * @author Dmitriy Moroz
     */
    override fun showGenerationSucceeded() {
        notificationManager.createAndShowInstant(
            LocalizationR.string.notification_finish_title.asUiText(),
            LocalizationR.string.notification_finish_sub_title.asUiText(),
        )
    }

    /**
     * Posts the localized generation-failed notification.
     *
     * @author Dmitriy Moroz
     */
    override fun showGenerationFailed() {
        notificationManager.createAndShowInstant(
            LocalizationR.string.notification_fail_title.asUiText(),
            LocalizationR.string.notification_fail_sub_title.asUiText(),
        )
    }
}
