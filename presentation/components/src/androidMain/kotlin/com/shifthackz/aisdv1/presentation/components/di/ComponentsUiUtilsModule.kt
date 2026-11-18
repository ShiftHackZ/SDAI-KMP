package com.shifthackz.aisdv1.presentation.components.di

import com.shifthackz.aisdv1.presentation.components.core.AndroidGenerationPlatformServices
import com.shifthackz.aisdv1.presentation.components.core.GenerationFormUpdateEvent
import com.shifthackz.aisdv1.presentation.components.core.GenerationPlatformServices
import com.shifthackz.aisdv1.presentation.components.image.AndroidImageSaver
import com.shifthackz.aisdv1.presentation.components.image.AndroidImageSharer
import com.shifthackz.aisdv1.presentation.components.image.ImageSaver
import com.shifthackz.aisdv1.presentation.components.image.ImageSharer
import com.shifthackz.aisdv1.presentation.components.utils.ReportProblemEmailComposer
import com.shifthackz.aisdv1.presentation.components.utils.ReportProblemEmailComposerImpl
import com.shifthackz.aisdv1.presentation.components.platform.StorageUsageFileSystem
import com.shifthackz.aisdv1.presentation.components.platform.AndroidStorageUsageFileSystem
import com.shifthackz.aisdv1.presentation.components.utils.OriginalImageFileExporter
import com.shifthackz.aisdv1.presentation.components.utils.StreamingOriginalImageFileExporter
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


/**
 * Installs Android image save/share/export singletons and factories for outcome notifications and report email.
 * Form-restoration events remain a shared singleton binding across screens.
 *
 * @author Dmitriy Moroz
 */
val componentsUiUtilsModule = module {
    single<OriginalImageFileExporter> { StreamingOriginalImageFileExporter(get(), get()) }
    factory<ReportProblemEmailComposer> { ReportProblemEmailComposerImpl(get(), get()) }
    single<StorageUsageFileSystem> { AndroidStorageUsageFileSystem(androidContext()) }
    factoryOf(::AndroidGenerationPlatformServices) bind GenerationPlatformServices::class
    single<ImageSaver> {
        AndroidImageSaver(
            mediaStoreGateway = get(),
            dispatchersProvider = get(),
        )
    }
    single<ImageSharer> {
        AndroidImageSharer(
            context = androidContext(),
            fileProviderDescriptor = get(),
            dispatchersProvider = get(),
            originals = get(),
        )
    }
    singleOf(::GenerationFormUpdateEvent)
}
