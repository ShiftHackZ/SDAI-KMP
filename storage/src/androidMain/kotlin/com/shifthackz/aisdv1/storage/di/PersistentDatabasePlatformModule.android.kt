package com.shifthackz.aisdv1.storage.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.shifthackz.aisdv1.storage.db.persistent.PersistentDatabase
import com.shifthackz.aisdv1.storage.gateway.GalleryImagePayloadReader
import com.shifthackz.aisdv1.storage.gateway.RoomGalleryImagePayloadReader
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Binds the application-owned Room database and its bulk image-payload query adapter.
 *
 * @author Dmitriy Moroz
 */
actual val persistentDatabasePlatformModule = module {
    single<GalleryImagePayloadReader> { RoomGalleryImagePayloadReader(get()) }
    single<RoomDatabase.Builder<PersistentDatabase>> {
        val context = androidContext().applicationContext
        Room.databaseBuilder<PersistentDatabase>(
            context = context,
            name = context.getDatabasePath(PersistentDatabase.DB_NAME).absolutePath,
        )
    }
}
