package com.shifthackz.aisdv1.storage.db.persistent

import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSessionEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSectionEntity
import com.shifthackz.aisdv1.storage.db.persistent.dao.RestoreSessionDao
import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.shifthackz.aisdv1.storage.converters.ListConverters
import com.shifthackz.aisdv1.storage.db.persistent.PersistentDatabase.Companion.DB_VERSION
import com.shifthackz.aisdv1.storage.db.persistent.dao.BenchmarkResultDao
import com.shifthackz.aisdv1.storage.db.persistent.contract.GenerationResultContract
import com.shifthackz.aisdv1.storage.db.persistent.contract.LocalModelContract
import com.shifthackz.aisdv1.storage.db.persistent.dao.GenerationResultDao
import com.shifthackz.aisdv1.storage.db.persistent.dao.HuggingFaceModelDao
import com.shifthackz.aisdv1.storage.db.persistent.dao.LocalModelDao
import com.shifthackz.aisdv1.storage.db.persistent.dao.NetworkUsageDao
import com.shifthackz.aisdv1.storage.db.persistent.dao.SupporterDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.BenchmarkResultEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.HuggingFaceModelEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.LocalModelEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.NetworkUsageEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.SupporterEntity

/**
 * Main Room database containing generated images, downloaded models, and app statistics.
 * Schema version 17 retains generation provenance, originating device and elapsed time without inventing legacy values.
 *
 * @author Dmitriy Moroz
 */
@Database(
    version = DB_VERSION,
    exportSchema = true,
    entities = [
        GenerationResultEntity::class,
        LocalModelEntity::class,
        HuggingFaceModelEntity::class,
        SupporterEntity::class,
        BenchmarkResultEntity::class,
        NetworkUsageEntity::class,
        RestoreSessionEntity::class,
        RestoreSectionEntity::class,
    ],
    autoMigrations = [
        /**
         * Added 3 fields to [GenerationResultEntity]:
         * - [GenerationResultContract.SUB_SEED]
         * - [GenerationResultContract.SUB_SEED_STRENGTH]
         * - [GenerationResultContract.DENOISING_STRENGTH]
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 1, to = 2),
        /**
         * Added [LocalModelEntity].
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 2, to = 3),
        /**
         * Added [HuggingFaceModelEntity].
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 3, to = 4),
        /**
         * Added [SupporterEntity].
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 4, to = 5),
        /**
         * Added 1 field to [LocalModelEntity]:
         * - [LocalModelContract.TYPE]
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 5, to = 6),
        /**
         * Added 1 field to [GenerationResultEntity]:
         * - [GenerationResultContract.HIDDEN]
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 6, to = 7),
        /**
         * Added index for gallery queries:
         * - [GenerationResultContract.CREATED_AT_INDEX]
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 7, to = 8),
        /**
         * Added 1 field to [GenerationResultEntity]:
         * - [GenerationResultContract.MODEL_NAME]
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 8, to = 9),
        /**
         * Added 1 field to [GenerationResultEntity]:
         * - [GenerationResultContract.LIKED]
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 9, to = 10),
        /**
         * Added [BenchmarkResultEntity].
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 10, to = 11),
        /**
         * Added [NetworkUsageEntity].
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 11, to = 12),
        /**
         * Added image content revisions used to invalidate gallery previews:
         * - [GenerationResultContract.IMAGE_REVISION]
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 12, to = 13),
        /**
         * Added durable restore progress tables:
         * - [RestoreSessionEntity]
         * - [RestoreSectionEntity]
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 13, to = 14),
        /**
         * Added restore handoff and retained-credential metadata:
         * - [RestoreSessionEntity.requiresConfiguration]
         * - [RestoreSectionEntity.retainedCredentialProviders]
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 14, to = 15),
        /**
         * Added nullable generation provenance; existing gallery rows retain unknown values:
         * - [GenerationResultEntity.providerKey]
         * - [GenerationResultEntity.acceleration]
         * - [GenerationResultEntity.generationDurationMillis]
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 15, to = 16),
        /**
         * Added nullable originating-device metadata without inferring it for existing images:
         * - [GenerationResultEntity.deviceName]
         * - [GenerationResultEntity.osVersion]
         *
         * @author Dmitriy Moroz
         */
        AutoMigration(from = 16, to = 17),
    ],
)
@TypeConverters(
    ListConverters::class,
)
@ConstructedBy(PersistentDatabaseConstructor::class)
internal abstract class PersistentDatabase : RoomDatabase() {
    /**
     * Reads and mutates saved gallery images and their metadata.
     *
     * @return The corresponding persistent table access owner.
     * @author Dmitriy Moroz
     */
    abstract fun generationResultDao(): GenerationResultDao

    /**
     * Tracks downloaded local model records without changing user-managed sources.
     *
     * @return The corresponding persistent table access owner.
     * @author Dmitriy Moroz
     */
    abstract fun localModelDao(): LocalModelDao

    /**
     * Reads saved Hugging Face model catalog records.
     *
     * @return The corresponding persistent table access owner.
     * @author Dmitriy Moroz
     */
    abstract fun huggingFaceModelDao(): HuggingFaceModelDao

    /**
     * Reads cached supporter records.
     *
     * @return The corresponding persistent table access owner.
     * @author Dmitriy Moroz
     */
    abstract fun supporterDao(): SupporterDao

    /**
     * Reads device-specific benchmark history.
     *
     * @return The corresponding persistent table access owner.
     * @author Dmitriy Moroz
     */
    abstract fun benchmarkResultDao(): BenchmarkResultDao

    /**
     * Returns the DAO used to persist and observe network traffic counters.
     *
     * @author Dmitriy Moroz
     */
    abstract fun networkUsageDao(): NetworkUsageDao

    /**
     * Returns durable restore intent and category-commit records.
     *
     * @author Dmitriy Moroz
     */
    abstract fun restoreSessionDao(): RestoreSessionDao

    companion object {
        /**
         * Installed database filename; preserve it across schema upgrades.
         *
         * @author Dmitriy Moroz
         */
        const val DB_NAME = "ai_sd_v1_storage_db"

        /**
         * Schema target including nullable image provenance, device information and elapsed time.
         *
         * @author Dmitriy Moroz
         */
        const val DB_VERSION = 17
    }
}

/**
 * Constructs native Room implementations for each supported target.
 *
 * @author Dmitriy Moroz
 */
@Suppress("KotlinNoActualForExpect")
internal expect object PersistentDatabaseConstructor : RoomDatabaseConstructor<PersistentDatabase> {
    /**
     * Creates the platform Room database implementation generated from this schema.
     *
     * @return Constructed persistent database owner.
     * @author Dmitriy Moroz
     */
    override fun initialize(): PersistentDatabase
}
