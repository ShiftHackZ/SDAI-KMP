package com.shifthackz.aisdv1.presentation.app.di

import com.shifthackz.aisdv1.presentation.components.di.componentsIosModule
import com.shifthackz.aisdv1.feature.sdxl.di.sdxlModule
import org.koin.dsl.module

/**
 * Installs iOS native backend selection and owner-module platform overrides.
 *
 * @author Dmitriy Moroz
 */
internal actual val platformPresentationModule = module {
    includes(sdxlModule, componentsIosModule)
}
