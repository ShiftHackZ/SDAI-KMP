package com.shifthackz.aisdv1.presentation.blog.detail.share

import org.koin.dsl.module

/**
 * Wires the blog to the iOS activity controller.
 *
 * @author Dmitriy Moroz
 */
internal actual val blogPlatformModule = module {
    single<BlogPlatformActions> { IosBlogPlatformActions() }
}
