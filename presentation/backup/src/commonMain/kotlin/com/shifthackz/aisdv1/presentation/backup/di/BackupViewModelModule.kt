package com.shifthackz.aisdv1.presentation.backup.di

import com.shifthackz.aisdv1.core.navigation.router.BackupRouter
import com.shifthackz.aisdv1.presentation.backup.BackupScreenMode
import com.shifthackz.aisdv1.presentation.backup.BackupViewModel
import com.shifthackz.aisdv1.domain.gateway.backup.BackupChangesGateway
import com.shifthackz.aisdv1.presentation.backup.service.BackupChangesGatewayImpl
import org.koin.dsl.module

/**
 * Creates caller-owned backup state holders with shared domain operations.
 * @author Dmitriy Moroz
 */
val backupViewModelModule = module {
    single<BackupChangesGateway> { BackupChangesGatewayImpl(get(), get()) }
    factory { (mode: BackupScreenMode, router: BackupRouter) ->
        BackupViewModel(
            mode,
            router,
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
