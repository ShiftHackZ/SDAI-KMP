package com.shifthackz.aisdv1.storage.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.shifthackz.aisdv1.storage.db.persistent.PersistentDatabase
import com.shifthackz.aisdv1.storage.gateway.GalleryImagePayloadReader
import com.shifthackz.aisdv1.storage.gateway.NativeGalleryImagePayloadReader
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.module
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * Binds Room's persistent path and the read-only native image-payload reader to the same database.
 *
 * @author Dmitriy Moroz
 */
actual val persistentDatabasePlatformModule = module {
    single<GalleryImagePayloadReader> {
        NativeGalleryImagePayloadReader(
            get(),
            "${applicationSupportDirectory()}/${PersistentDatabase.DB_NAME}"
        )
    }
    single<RoomDatabase.Builder<PersistentDatabase>> {
        Room.databaseBuilder<PersistentDatabase>(
            name = "${applicationSupportDirectory()}/${PersistentDatabase.DB_NAME}",
        )
    }
}

/**
 * Resolves and creates the application-owned support directory shared by persistent database access.
 *
 * @return Absolute application-support directory path.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalForeignApi::class)
private fun applicationSupportDirectory(): String {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(directory?.path)
}
