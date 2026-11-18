package com.shifthackz.aisdv1.presentation.app.bootstrap

import android.app.Application
import androidx.work.WorkerFactory
import com.shifthackz.aisdv1.core.common.appbuild.ActivityIntentProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.common.links.LinksProvider
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.common.time.TimeProvider
import com.shifthackz.aisdv1.core.imageprocessing.di.imageProcessingModule
import com.shifthackz.aisdv1.core.notification.di.notificationModule
import com.shifthackz.aisdv1.core.mvi.diagnostics.uiDiagnosticsModule
import com.shifthackz.aisdv1.domain.di.domainModule
import com.shifthackz.aisdv1.domain.feature.sdxl.StableDiffusionCpp
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.gateway.ProcessExitDiagnosticsGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.feature.openaioauth.di.openAiOAuthModule
import com.shifthackz.aisdv1.presentation.app.di.commonApplicationModules
import com.shifthackz.aisdv1.presentation.app.di.initializeApplicationLanguage
import com.shifthackz.aisdv1.presentation.app.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Starts the process-global Android graph; the native application calls this once in the host process.
 * Platform bindings are registered last, before services are resolved. Child inference processes
 * use the separate inference bootstrap and must not call this entrypoint.
 *
 * @param application Native application context used by Android adapters.
 * @param bindings Lazily supplied native integrations; inference runtimes stay in child graphs.
 * @param isDiagnosticsEnabled Whether this host build permits UI diagnostics.
 *
 * @return Typed services whose resolution follows the host startup lifecycle.
 *
 * @author Dmitriy Moroz
 */
fun startAndroidApplication(
    application: Application,
    bindings: AndroidHostBindings,
    isDiagnosticsEnabled: Boolean,
): AndroidAppServices {
    val graph = startKoin {
        androidContext(application)
        modules(commonApplicationModules)
        modules(notificationModule, imageProcessingModule, openAiOAuthModule)
        modules(*domainModule, *presentationModule)
        modules(uiDiagnosticsModule(isEnabled = isDiagnosticsEnabled), module {
            single<LinksProvider> { bindings.linksProvider }
            single<BuildInfoProvider> { bindings.buildInfoProvider }
            single<DispatchersProvider> { bindings.dispatchersProvider }
            single<TimeProvider> { bindings.timeProvider }
            single<FileProviderDescriptor> { bindings.fileProviderDescriptor }
            single<ActivityIntentProvider> { bindings.activityIntentProvider }
            single<NativeGenerationGateway> { bindings.nativeGenerationGateway }
            single<ProcessExitDiagnosticsGateway> { bindings.processExitDiagnosticsGateway }
            single<StableDiffusionCpp> { bindings.stableDiffusionCpp }
        })
    }.koin
    initializeApplicationLanguage(graph)
    return object : AndroidAppServices {
        override val workerFactory: WorkerFactory get() = graph.get()
        override val preferenceManager: PreferenceManager get() = graph.get()
    }
}
