@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.navigation

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo

/**
 * Lets modern UIKit bars float while older iOS keeps its opaque bottom layout.
 *
 * @author Dmitriy Moroz
 */
actual val isPlatformNavigationBarFloating: Boolean
    get() = NSProcessInfo.processInfo.operatingSystemVersion.useContents { majorVersion >= 26 }
