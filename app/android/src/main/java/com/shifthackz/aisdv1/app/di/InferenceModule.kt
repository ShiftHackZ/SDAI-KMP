package com.shifthackz.aisdv1.app.di

import com.shifthackz.aisdv1.app.diagnostics.AndroidProcessExitDiagnosticsGateway
import com.shifthackz.aisdv1.app.diagnostics.FileGenerationAttemptJournal
import com.shifthackz.aisdv1.app.diagnostics.GenerationAttemptJournal
import com.shifthackz.aisdv1.app.inference.AndroidInferenceConnectionFactory
import com.shifthackz.aisdv1.app.inference.FileGenerationFileStore
import com.shifthackz.aisdv1.app.inference.GenerationFileStore
import com.shifthackz.aisdv1.app.inference.PngImageValidator
import com.shifthackz.aisdv1.app.inference.CompletePngImageValidator
import com.shifthackz.aisdv1.app.inference.InferenceConnectionFactory
import com.shifthackz.aisdv1.app.inference.InferenceServiceDependencies
import com.shifthackz.aisdv1.app.inference.NativeGenerationGatewayImpl
import com.shifthackz.aisdv1.app.inference.NativeInferenceEngineFactory
import com.shifthackz.aisdv1.app.inference.NativeInferenceEngineFactoryImpl
import com.shifthackz.aisdv1.app.inference.RemoteStableDiffusionCpp
import com.shifthackz.aisdv1.domain.feature.sdxl.StableDiffusionCpp
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.gateway.ProcessExitDiagnosticsGateway
import org.koin.android.ext.koin.androidContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Cheap host transport collaborators; native feature modules are absent from this graph.
 *
 * @author Dmitriy Moroz
 */
internal val inferenceHostModule = module {
    singleOf(::CompletePngImageValidator) bind PngImageValidator::class
    single<GenerationFileStore> { FileGenerationFileStore(androidContext(), get()) }
    single<GenerationAttemptJournal> { FileGenerationAttemptJournal(androidContext()) }
    single<InferenceConnectionFactory> { AndroidInferenceConnectionFactory(androidContext()) }
    single<ProcessExitDiagnosticsGateway> { AndroidProcessExitDiagnosticsGateway(androidContext()) }
    single<NativeGenerationGateway> {
        NativeGenerationGatewayImpl(
            get(),
            get(),
            get(),
            get(),
            CoroutineScope(SupervisorJob() + Dispatchers.IO)
        )
    }
    singleOf(::RemoteStableDiffusionCpp) bind StableDiffusionCpp::class
}

/**
 * Minimal child graph excludes WorkManager, Room, preferences, auth and presentation.
 *
 * @author Dmitriy Moroz
 */
internal val inferenceChildModule = module {
    singleOf(::CompletePngImageValidator) bind PngImageValidator::class
    single<GenerationFileStore> { FileGenerationFileStore(androidContext(), get()) }
    single<NativeInferenceEngineFactory> { NativeInferenceEngineFactoryImpl(androidContext()) }
    singleOf(::InferenceServiceDependencies)
}
