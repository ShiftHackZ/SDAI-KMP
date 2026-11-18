package com.shifthackz.aisdv1.presentation.report.di

import com.shifthackz.aisdv1.core.navigation.router.ReportRouter
import com.shifthackz.aisdv1.presentation.report.ReportViewModel
import org.koin.dsl.module


/**
 * Creates report state holders for the selected generation identity and report navigation owner.
 *
 * @author Dmitriy Moroz
 */
val reportViewModelModule = module {
    factory { (itemId: Long, router: ReportRouter) ->
        ReportViewModel(
            diagnostics = get(),
            itemId = itemId,
            dispatchersProvider = get(),
            sendReportUseCase = get(),
            getGenerationResultUseCase = get(),
            getLastResultFromCacheUseCase = get(),
            router = router,
            buildInfoProvider = get(),
        )
    }
}
