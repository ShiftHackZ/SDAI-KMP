package com.shifthackz.aisdv1.presentation.blog.detail.share

import org.koin.dsl.module

/**
 * Wires blog sharing to Android's system chooser.
 *
 * @author Dmitriy Moroz
 */
internal actual val blogPlatformModule = module {
    single<BlogPlatformActions> { AndroidBlogPlatformActions(get()) }
}
