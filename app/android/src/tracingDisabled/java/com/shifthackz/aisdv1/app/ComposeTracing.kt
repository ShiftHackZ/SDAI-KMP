package com.shifthackz.aisdv1.app

import android.content.Context

/**
 * Keeps ordinary builds independent of the optional Compose runtime tracing dependency.
 *
 * @param context Application context accepted by the variant composition root.
 * @author Dmitriy Moroz
 */
@Suppress("UNUSED_PARAMETER")
internal fun initializeComposeTracing(context: Context) = Unit
