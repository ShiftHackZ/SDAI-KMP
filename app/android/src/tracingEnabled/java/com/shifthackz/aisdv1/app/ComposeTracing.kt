package com.shifthackz.aisdv1.app

import android.content.Context
import androidx.compose.runtime.tracing.ComposeTracingInitializer

/**
 * Registers the optional Compose tracer in the host process without restoring startup providers.
 * Recording still requires an explicit Perfetto tracing session and SDK enablement.
 *
 * @param context Application context used by the AndroidX tracing initializer.
 * @author Dmitriy Moroz
 */
internal fun initializeComposeTracing(context: Context) {
    ComposeTracingInitializer().create(context)
}
