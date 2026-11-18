package com.shifthackz.aisdv1.presentation.app.di

import org.koin.dsl.module

/**
 * Completes the shared platform-module contract without registering Android bindings.
 * Android host and inference-process dependencies are supplied through the typed bootstrap modules.
 *
 * @author Dmitriy Moroz
 */
internal actual val platformPresentationModule = module {}
