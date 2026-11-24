package com.shifthackz.aisdv1.presentation.app.di

import com.shifthackz.aisdv1.presentation.benchmark.di.benchmarkCoreModule
import com.shifthackz.aisdv1.presentation.benchmark.di.benchmarkViewModelModule
import com.shifthackz.aisdv1.presentation.statistics.di.statisticsViewModelModule
import com.shifthackz.aisdv1.presentation.settings.di.settingsCoreModule
import com.shifthackz.aisdv1.presentation.settings.di.settingsViewModelModule
import com.shifthackz.aisdv1.presentation.configuration.di.configurationViewModelModule
import com.shifthackz.aisdv1.presentation.splash.di.splashViewModelModule
import com.shifthackz.aisdv1.presentation.donate.di.donateViewModelModule
import com.shifthackz.aisdv1.presentation.logger.di.loggerViewModelModule
import com.shifthackz.aisdv1.presentation.logger.di.loggerCoreModule
import com.shifthackz.aisdv1.presentation.faq.di.faqViewModelModule
import com.shifthackz.aisdv1.presentation.web.di.webViewModelModule
import com.shifthackz.aisdv1.presentation.gallery.di.galleryCoreModule
import com.shifthackz.aisdv1.presentation.gallery.di.galleryViewModelModule
import com.shifthackz.aisdv1.presentation.components.di.componentsCoreModule
import com.shifthackz.aisdv1.presentation.components.di.componentsViewModelModule
import com.shifthackz.aisdv1.presentation.history.di.historyViewModelModule
import com.shifthackz.aisdv1.presentation.report.di.reportViewModelModule
import com.shifthackz.aisdv1.presentation.onboarding.di.onboardingViewModelModule
import com.shifthackz.aisdv1.presentation.generation.di.generationCoreModule
import com.shifthackz.aisdv1.presentation.generation.di.generationViewModelModule
import com.shifthackz.aisdv1.presentation.debug.di.debugCoreModule
import com.shifthackz.aisdv1.presentation.debug.di.debugViewModelModule
import com.shifthackz.aisdv1.presentation.backup.di.backupViewModelModule
import org.koin.dsl.module

/**
 * Aggregates owner-module registrations in the application graph.
 *
 * @author Dmitriy Moroz
 */
val corePresentationModule = module {
    includes(
        appViewModelModule,
        backupViewModelModule,
        benchmarkCoreModule,
        benchmarkViewModelModule,
        statisticsViewModelModule,
        settingsCoreModule,
        settingsViewModelModule,
        configurationViewModelModule,
        splashViewModelModule,
        donateViewModelModule,
        loggerViewModelModule,
        loggerCoreModule,
        appCoreModule,
        webViewModelModule,
        faqViewModelModule,
        galleryCoreModule,
        galleryViewModelModule,
        componentsCoreModule,
        componentsViewModelModule,
        historyViewModelModule,
        reportViewModelModule,
        onboardingViewModelModule,
        generationCoreModule,
        generationViewModelModule,
        debugCoreModule,
        debugViewModelModule
    )
}
