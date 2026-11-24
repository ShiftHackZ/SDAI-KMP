package com.shifthackz.aisdv1.presentation.blog.di

import com.shifthackz.aisdv1.presentation.blog.detail.BlogDetailViewModel
import com.shifthackz.aisdv1.presentation.blog.detail.share.blogPlatformModule
import com.shifthackz.aisdv1.presentation.blog.list.BlogViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Supplies an independent ViewModel for each blog destination and binds native sharing.
 *
 * @author Dmitriy Moroz
 */
val blogViewModelModule = module {
    includes(blogPlatformModule)
    factoryOf(::BlogViewModel)
    factory { (slug: String) ->
        BlogDetailViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            fetchOrGetBlogPost = get(),
            platformActions = get(),
            slug = slug,
        )
    }
}
