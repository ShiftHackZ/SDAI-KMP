package com.shifthackz.aisdv1.presentation.app.bootstrap

import android.content.Context

/**
 * Creates the child-only factory without starting any host graph or loading a runtime.
 *
 * @param context Child process application context.
 * @param isDiagnosticsEnabled Whether per-job runtime diagnostics are enabled.
 *
 * @return Factory creating separately owned inference graphs per job.
 *
 * @author Dmitriy Moroz
 */
fun createNativeInferenceEngineFactory(
    context: Context,
    isDiagnosticsEnabled: Boolean
): NativeInferenceEngineFactory =
    NativeInferenceEngineFactoryImpl(context, isDiagnosticsEnabled)
