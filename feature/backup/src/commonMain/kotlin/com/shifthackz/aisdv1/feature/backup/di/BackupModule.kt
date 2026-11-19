package com.shifthackz.aisdv1.feature.backup.di

import com.shifthackz.aisdv1.feature.backup.archive.BackupArchiveStore
import com.shifthackz.aisdv1.feature.backup.archive.BackupStagingCleaner
import com.shifthackz.aisdv1.feature.backup.archive.DefaultBackupStagingCleaner
import com.shifthackz.aisdv1.feature.backup.archive.DefaultBackupArchiveStore
import com.shifthackz.aisdv1.feature.backup.codec.BackupArchiveCodec
import com.shifthackz.aisdv1.feature.backup.codec.BinaryBackupArchiveCodec
import com.shifthackz.aisdv1.feature.backup.codec.BackupPreferencesCodec
import com.shifthackz.aisdv1.feature.backup.codec.DefaultBackupPreferencesCodec
import com.shifthackz.aisdv1.feature.backup.codec.GalleryBackupCodec
import com.shifthackz.aisdv1.feature.backup.codec.DefaultGalleryBackupCodec
import com.shifthackz.aisdv1.feature.backup.restore.BackupProviderPlanner
import com.shifthackz.aisdv1.feature.backup.restore.DefaultBackupProviderPlanner
import com.shifthackz.aisdv1.feature.backup.restore.BackupRestoreGuard
import com.shifthackz.aisdv1.feature.backup.restore.DefaultBackupRestoreGuard
import com.shifthackz.aisdv1.feature.backup.restore.BackupRestoreWriter
import com.shifthackz.aisdv1.feature.backup.restore.RoomBackupRestoreWriter
import com.shifthackz.aisdv1.feature.backup.restore.BackupSessionCodec
import com.shifthackz.aisdv1.feature.backup.restore.DefaultBackupSessionCodec
import com.shifthackz.aisdv1.domain.usecase.backup.LoadBackupInventoryUseCase
import com.shifthackz.aisdv1.feature.backup.usecase.LoadBackupInventoryUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.backup.CreateBackupUseCase
import com.shifthackz.aisdv1.feature.backup.usecase.CreateBackupUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.backup.InspectBackupUseCase
import com.shifthackz.aisdv1.feature.backup.usecase.InspectBackupUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.backup.RestoreBackupUseCase
import com.shifthackz.aisdv1.feature.backup.usecase.RestoreBackupUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.backup.ExportBackupUseCase
import com.shifthackz.aisdv1.feature.backup.usecase.ExportBackupUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.backup.DiscardBackupUseCase
import com.shifthackz.aisdv1.feature.backup.usecase.DiscardBackupUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.backup.GetBackupStartupStageUseCase
import com.shifthackz.aisdv1.feature.backup.usecase.GetBackupStartupStageUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.backup.SetBackupStartupStageUseCase
import com.shifthackz.aisdv1.feature.backup.usecase.SetBackupStartupStageUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.backup.LoadPendingBackupUseCase
import com.shifthackz.aisdv1.feature.backup.usecase.LoadPendingBackupUseCaseImpl
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import com.shifthackz.aisdv1.domain.gateway.GenerationRestoreGate
import com.shifthackz.aisdv1.feature.backup.restore.DefaultGenerationRestoreGate
import com.shifthackz.aisdv1.domain.usecase.backup.CompleteBackupExportUseCase
import com.shifthackz.aisdv1.feature.backup.usecase.CompleteBackupExportUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.backup.ObserveBackupExportUseCase
import com.shifthackz.aisdv1.feature.backup.usecase.ObserveBackupExportUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.backup.CancelBackupExportUseCase
import com.shifthackz.aisdv1.feature.backup.usecase.CancelBackupExportUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.backup.DismissBackupExportUseCase
import com.shifthackz.aisdv1.feature.backup.usecase.DismissBackupExportUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.backup.GetPendingBackupExportUseCase
import com.shifthackz.aisdv1.feature.backup.usecase.GetPendingBackupExportUseCaseImpl
import com.shifthackz.aisdv1.feature.backup.export.BackupExportCoordinator
import com.shifthackz.aisdv1.feature.backup.export.DefaultBackupExportCoordinator
import org.koin.dsl.module
import org.koin.dsl.onClose

/**
 * Installs platform file transport behind the same common backup contracts on both targets.
 * @author Dmitriy Moroz
 */
internal expect val backupPlatformModule: Module

/**
 * Registers backup implementations without exposing feature dependencies to presentation leaves.
 * @author Dmitriy Moroz
 */
val backupModule = module {
    includes(backupPlatformModule)
    single<GenerationRestoreGate> { DefaultGenerationRestoreGate() }
    single(named("backupJson")) { Json { ignoreUnknownKeys = true; encodeDefaults = true } }
    single<BackupArchiveCodec> { BinaryBackupArchiveCodec(get(named("backupJson"))) }
    single<BackupPreferencesCodec> { DefaultBackupPreferencesCodec(get(named("backupJson"))) }
    single<GalleryBackupCodec> {
        DefaultGalleryBackupCodec(
            get(),
            get(named("backupJson")),
            get(),
            get()
        )
    }
    single<BackupRestoreGuard> { DefaultBackupRestoreGuard() }
    single<BackupProviderPlanner> { DefaultBackupProviderPlanner(get()) }
    single<BackupSessionCodec> { DefaultBackupSessionCodec(get(named("backupJson"))) }
    single<BackupRestoreWriter> {
        RoomBackupRestoreWriter(
            get(),
            get(),
            get(),
            get(),
            get(named("backupJson"))
        )
    }
    single<BackupStagingCleaner> { DefaultBackupStagingCleaner(get(), get(), get()) }
    single<BackupArchiveStore> {
        DefaultBackupArchiveStore(
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
            get(named("backupJson")),
            get()
        )
    }
    factory<LoadBackupInventoryUseCase> { LoadBackupInventoryUseCaseImpl(get(), get()) }
    single<BackupExportCoordinator> {
        DefaultBackupExportCoordinator(
            get(),
            get(),
            get(),
            get()
        )
    } onClose { it?.close() }
    factory<CompleteBackupExportUseCase> { CompleteBackupExportUseCaseImpl(get()) }
    factory<ObserveBackupExportUseCase> { ObserveBackupExportUseCaseImpl(get()) }
    factory<CancelBackupExportUseCase> { CancelBackupExportUseCaseImpl(get()) }
    factory<DismissBackupExportUseCase> { DismissBackupExportUseCaseImpl(get()) }
    factory<GetPendingBackupExportUseCase> { GetPendingBackupExportUseCaseImpl(get(), get()) }
    factory<CreateBackupUseCase> { CreateBackupUseCaseImpl(get(), get()) }
    factory<InspectBackupUseCase> { InspectBackupUseCaseImpl(get(), get()) }
    factory<ExportBackupUseCase> { ExportBackupUseCaseImpl(get(), get()) }
    factory<GetBackupStartupStageUseCase> { GetBackupStartupStageUseCaseImpl(get()) }
    factory<SetBackupStartupStageUseCase> { SetBackupStartupStageUseCaseImpl(get()) }
    factory<LoadPendingBackupUseCase> {
        LoadPendingBackupUseCaseImpl(
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
    factory<DiscardBackupUseCase> {
        DiscardBackupUseCaseImpl(
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
    factory<RestoreBackupUseCase> {
        RestoreBackupUseCaseImpl(
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
