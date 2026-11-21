package com.shifthackz.aisdv1.domain.usecase.generation

import platform.Foundation.NSProcessInfo

internal actual fun bonsaiAcceleration(requestedMode: String): String = "Metal"

internal actual fun coreMlAcceleration(): String =
    if (NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null) "CPU + GPU" else "CPU + Neural Engine"
