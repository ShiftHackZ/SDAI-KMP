package com.shifthackz.aisdv1.feature.civitai.di

import com.shifthackz.aisdv1.domain.usecase.civitai.UploadToCivitaiUseCase
import com.shifthackz.aisdv1.domain.usecase.civitai.AutoUploadToCivitaiUseCase
import com.shifthackz.aisdv1.feature.civitai.UploadToCivitaiUseCaseImpl
import com.shifthackz.aisdv1.feature.civitai.AutoUploadToCivitaiUseCaseImpl
import com.shifthackz.aisdv1.feature.civitai.CivitaiDescription
import com.shifthackz.aisdv1.feature.civitai.CivitaiDescriptionImpl
import com.shifthackz.aisdv1.domain.usecase.civitai.SignInToCivitaiUseCase
import com.shifthackz.aisdv1.feature.civitai.SignInToCivitaiUseCaseImpl
import org.koin.dsl.module

/**
 * Shared Android/iOS publication policy and single-flight owner.
 *
 * @author Dmitriy Moroz
 */
val civitaiModule = module {
    includes(civitaiPlatformModule)
    factory<SignInToCivitaiUseCase> { SignInToCivitaiUseCaseImpl(get(), get()) }
    factory<CivitaiDescription> { CivitaiDescriptionImpl() }
    single<UploadToCivitaiUseCase> { UploadToCivitaiUseCaseImpl(get(), get(), get()) }
    factory<AutoUploadToCivitaiUseCase> { AutoUploadToCivitaiUseCaseImpl(get(), get(), get()) }
}
