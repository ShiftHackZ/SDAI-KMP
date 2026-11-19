package com.shifthackz.aisdv1.presentation.app.di

import com.shifthackz.aisdv1.presentation.benchmark.di.benchmarkUiUtilsModule
import com.shifthackz.aisdv1.presentation.logger.di.loggerUiUtilsModule
import com.shifthackz.aisdv1.presentation.gallery.di.galleryUiUtilsModule
import com.shifthackz.aisdv1.presentation.components.di.componentsUiUtilsModule
import com.shifthackz.aisdv1.presentation.debug.di.debugUiUtilsModule

/**
 * Aggregates owner-module registrations in the application graph.
 *
 * @author Dmitriy Moroz
 */
val presentationModule = arrayOf(
    benchmarkUiUtilsModule,
    loggerUiUtilsModule,
    galleryUiUtilsModule,
    componentsUiUtilsModule,
    debugUiUtilsModule
)
