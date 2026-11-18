package com.shifthackz.aisdv1.presentation.activity

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.shifthackz.aisdv1.core.common.log.debugLog
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.presentation.app.AiSdApp
import com.shifthackz.aisdv1.presentation.components.utils.PermissionUtil
import com.shifthackz.aisdv1.app.AiStableDiffusionClientApp

/**
 * Hosts the shared Compose application and defers startup permission prompts until the splash fade completes.
 *
 * @author Dmitriy Moroz
 */
class AiStableDiffusionActivity : AppCompatActivity() {

    /**
     * Reads the shared application preferences used by startup permission callbacks.
     *
     * @author Dmitriy Moroz
     */
    private val preferenceManager: PreferenceManager
        get() = (application as AiStableDiffusionClientApp).appServices.preferenceManager

    /**
     * Logs the result of the startup notification permission request.
     *
     * @author Dmitriy Moroz
     */
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        debugLog("Notification permission is ${if (granted) "GRANTED" else "DENIED"}.")
    }

    /**
     * Enables media-store saving after every requested storage permission is granted.
     *
     * @author Dmitriy Moroz
     */
    private val storagePermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (!result.values.any { !it }) {
            preferenceManager.saveToMediaStore = true
        }
        debugLog("Storage permission is ${result}.")
    }

    /**
     * Installs the splash exit animation, schedules permission prompts and renders the shared application.
     *
     * @param savedInstanceState Android state restored by the superclass before shared content is installed.
     *
     * @author Dmitriy Moroz
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val fadeOutAnimation = ObjectAnimator.ofFloat(
                splashScreenViewProvider.view,
                View.ALPHA,
                1f,
                0f
            )
            fadeOutAnimation.duration = 500L
            fadeOutAnimation.doOnEnd {
                PermissionUtil.checkNotificationPermission(this, notificationPermission::launch)
                PermissionUtil.checkStoragePermission(this, storagePermission::launch)
                splashScreenViewProvider.remove()
            }
            fadeOutAnimation.start()
        }
        setContent {
            AiSdApp()
        }
    }
}
