package com.shifthackz.aisdv1.presentation.appupdate

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Presents Play's consent and installation UI, resuming an interrupted immediate update.
 * Play owns download, installation and restart; declining returns to the usable application.
 *
 * @param onFinished Consumes this launch's notification on result, ineligibility or SDK failure.
 * @author Dmitriy Moroz
 */
@Composable
internal fun DistributionAppUpdateHost(onFinished: () -> Unit) {
    val context = LocalContext.current
    val manager = remember(context) { AppUpdateManagerFactory.create(context) }
    val currentOnFinished = rememberUpdatedState(onFinished)
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            currentOnFinished.value()
        }
    val isActive = remember { mutableStateOf(true) }
    val hasRequestedFlow = remember { mutableStateOf(false) }
    DisposableEffect(manager) {
        isActive.value = true
        onDispose { isActive.value = false }
    }
    val startUpdate = remember(manager, launcher) {
        { shouldResumeOnly: Boolean ->
            if (!hasRequestedFlow.value) manager.appUpdateInfo.addOnSuccessListener { info ->
                if (!isActive.value || hasRequestedFlow.value) return@addOnSuccessListener
                val isInProgress =
                    info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                val isAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                if ((isInProgress || (!shouldResumeOnly && isAvailable)) && info.isUpdateTypeAllowed(
                        AppUpdateType.IMMEDIATE
                    )
                ) {
                    try {
                        hasRequestedFlow.value = true
                        val hasStarted = manager.startUpdateFlowForResult(
                            info,
                            launcher,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                        )
                        if (!hasStarted) currentOnFinished.value()
                    } catch (_: Exception) {
                        currentOnFinished.value()
                    }
                } else if (!shouldResumeOnly) currentOnFinished.value()
            }.addOnFailureListener { if (isActive.value) currentOnFinished.value() }
            Unit
        }
    }
    LaunchedEffect(manager) { startUpdate(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { startUpdate(true) }
}
