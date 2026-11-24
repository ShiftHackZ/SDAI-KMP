package com.shifthackz.aisdv1.presentation.faq.di

import com.shifthackz.aisdv1.presentation.faq.FaqViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Supplies one ViewModel per FAQ screen visit.
 *
 * @author Dmitriy Moroz
 */
val faqViewModelModule = module {
    factoryOf(::FaqViewModel)
}
