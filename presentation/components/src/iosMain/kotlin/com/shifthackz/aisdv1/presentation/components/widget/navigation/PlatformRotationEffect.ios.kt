package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientationDidChangeNotification
import platform.UIKit.UIDeviceOrientationIsValidInterfaceOrientation

/**
 * Balances device orientation notifications with the composition lifetime and ignores flat positions.
 * Physical rotation remains observable when adaptive iOS keeps the application's window dimensions.
 *
 * @param onRotate Current UI callback, updated without replacing the native observer.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformRotationEffect(onRotate: () -> Unit) {
    val currentOnRotate by rememberUpdatedState(onRotate)
    DisposableEffect(Unit) {
        val device = UIDevice.currentDevice
        var previousOrientation = device.orientation
        device.beginGeneratingDeviceOrientationNotifications()
        val center = NSNotificationCenter.defaultCenter
        val observer = center.addObserverForName(
            UIDeviceOrientationDidChangeNotification,
            device,
            NSOperationQueue.mainQueue,
        ) {
            val orientation = device.orientation
            if (UIDeviceOrientationIsValidInterfaceOrientation(orientation) &&
                orientation != previousOrientation
            ) {
                previousOrientation = orientation
                currentOnRotate()
            }
        }
        onDispose {
            center.removeObserver(observer)
            device.endGeneratingDeviceOrientationNotifications()
        }
    }
}
