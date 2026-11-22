package com.shifthackz.aisdv1.feature.backup.archive

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.time.TimeProvider
import com.shifthackz.aisdv1.domain.entity.backup.BackupPreferences
import com.shifthackz.aisdv1.domain.entity.backup.BackupValue
import com.shifthackz.aisdv1.domain.entity.backup.BackupImageBudget
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.gateway.backup.BackupPreferencesGateway
import com.shifthackz.aisdv1.domain.gateway.backup.BackupImageValidator
import com.shifthackz.aisdv1.feature.backup.codec.DefaultCivitaiBackupCodec
import com.shifthackz.aisdv1.storage.db.persistent.dao.CivitaiUploadDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.CivitaiUploadEntity
import com.shifthackz.aisdv1.feature.backup.codec.BinaryBackupArchiveCodec
import com.shifthackz.aisdv1.feature.backup.codec.BackupManifest
import com.shifthackz.aisdv1.feature.backup.codec.BackupSection
import com.shifthackz.aisdv1.feature.backup.codec.DefaultBackupPreferencesCodec
import com.shifthackz.aisdv1.feature.backup.codec.DefaultGalleryBackupCodec
import com.shifthackz.aisdv1.feature.backup.io.BackupFileStore
import com.shifthackz.aisdv1.storage.db.persistent.dao.GenerationResultDao
import com.shifthackz.aisdv1.storage.db.persistent.dao.NetworkUsageDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.NetworkUsageEntity
import com.shifthackz.aisdv1.storage.gateway.BackupStorage
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer

/**
 * Exercises real archive framing, codecs and private files with isolated persistence/transport collaborators.
 *
 * @param root Private temporary directory owned by one unit-test case.
 * @author Dmitriy Moroz
 */
internal class BackupArchiveFixture(val root: Path) {
    val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }
    val codec = BinaryBackupArchiveCodec(json)
    val fields = DefaultBackupPreferencesCodec(json)
    val preferences = mockk<BackupPreferencesGateway>()
    val authorization = mockk<AuthorizationStore>()
    val images = mockk<GenerationResultDao>()
    val receipts = mockk<CivitaiUploadDao>()
    var uploads = emptyList<CivitaiUploadEntity>()
    val network = mockk<NetworkUsageDao>()
    val database = mockk<BackupStorage>()
    val staging = mockk<BackupStagingCleaner>(relaxed = true)
    val files = mockk<BackupFileStore>()
    var snapshot = BackupPreferences(emptyMap(), mapOf("autoSave" to BackupValue.Flag(true)))
    var rows = emptyList<GenerationResultEntity>()
    var counters = emptyList<NetworkUsageEntity>()
    var displayName = "selected.sdaibackup"
    var location = "/Downloads/selected.sdaibackup"
    private var nextId = 1

    init {
        FileSystem.SYSTEM.createDirectories(root)
        every { files.root } returns root
        every { files.newId() } answers {
            "00000000-0000-0000-0000-${
                (nextId++).toString().padStart(12, '0')
            }"
        }
        every { files.documentReference(any()) } answers { "file://${firstArg<Path>()}" }
        every { files.documentPath(any()) } answers {
            firstArg<String>().removePrefix("file://").toPath()
        }
        every { files.documentName(any()) } answers { displayName }
        every { files.documentLocation(any()) } answers { location }
        every { files.openDocument(any()) } answers {
            FileSystem.SYSTEM.source(
                firstArg<String>().removePrefix(
                    "file://"
                ).toPath()
            ).buffer()
        }
        every { files.writeDocument(any()) } answers {
            FileSystem.SYSTEM.sink(
                firstArg<String>().removePrefix(
                    "file://"
                ).toPath()
            ).buffer()
        }
        every { files.availableBytes() } returns Long.MAX_VALUE
        every { preferences.read() } answers { snapshot }
        every { preferences.filterSupported(any()) } answers { firstArg() }
        coEvery { authorization.getAuthorizationCredentials() } returns AuthorizationCredentials.None
        every { database.generationResultDao() } returns images
        every { database.networkUsageDao() } returns network
        every { database.civitaiUploadDao() } returns receipts
        coEvery { receipts.count() } answers { uploads.size.toLong() }
        coEvery { receipts.queryBackupPage(any(), any()) } answers {
            uploads.filter { it.id > firstArg<Long>() }.sortedBy { it.id }.take(secondArg())
        }
        coEvery { database.readSnapshot<Unit>(any()) } coAnswers { firstArg<suspend () -> Unit>()() }
        every { images.observeCount() } answers { flowOf(rows.size) }
        coEvery {
            images.queryBackupPage(
                any(),
                any()
            )
        } answers { rows.filter { it.id > firstArg<Long>() }.sortedBy { it.id }.take(secondArg()) }
        coEvery { network.query() } answers { counters }
    }

    val galleryCodec = DefaultGalleryBackupCodec(
        codec,
        json,
        mockk<BackupImageValidator>(relaxed = true),
        BackupImageBudget(1024, 100, 4096, 16)
    )
    val store = DefaultBackupArchiveStore(
        files,
        codec,
        galleryCodec,
        fields,
        database,
        preferences,
        authorization,
        ProviderSelectionGuard(),
        BuildInfoProvider.stub,
        mockk<TimeProvider> { every { currentTimeMillis() } returns 123L },
        json,
        staging,
        DefaultCivitaiBackupCodec(codec, json),
    )

    /**
     * Creates a checksum-valid archive with one framed JSON section for compatibility/admission cases.
     *
     * @param id Stable or future section identifier.
     * @param text JSON payload consumed by the section codec.
     * @param transform Descriptor mutation for versions or record-count mismatches.
     * @author Dmitriy Moroz
     */
    suspend fun document(
        id: String,
        text: String,
        transform: (BackupSection) -> BackupSection = { it },
    ): String {
        val payload = root.parent!! / "payload"
        FileSystem.SYSTEM.sink(payload).buffer().use { codec.writeJson(it, text) }
        val section = transform(codec.describe(payload, id, 1))
        val document = root.parent!! / "source.sdaibackup"
        codec.write(
            document,
            BackupManifest("snapshot", 123, "1", "IOS", listOf(section)),
            listOf(payload)
        )
        return "file://$document"
    }

    /**
     * Supplies portable gallery metadata; native image validation is isolated by the fixture collaborator.
     *
     * @param id Existing row identity, deliberately excluded by the portable codec.
     * @author Dmitriy Moroz
     */
    fun row(id: Long) = GenerationResultEntity(
        id, "iVBORw0KGgoAAAAA", "", 100, "txt2img", "prompt", "negative", 1, 1, 0, 7.5f,
        false, "Euler", "42", "9", 0.25f, 0.5f, true, true, "model", "old-revision",
    )

    /**
     * Writes one valid gallery record while allowing an inconsistent declared count for admission testing.
     *
     * @param declaredCount Number of gallery records advertised in the checked manifest.
     * @author Dmitriy Moroz
     */
    suspend fun galleryDocument(declaredCount: Long): String {
        val payload = root.parent!! / "gallery-payload"
        FileSystem.SYSTEM.sink(payload).buffer().use { galleryCodec.write(it, row(71)) }
        val section = codec.describe(payload, "gallery", declaredCount)
        val document = root.parent!! / "gallery-source.sdaibackup"
        codec.write(
            document,
            BackupManifest("snapshot", 123, "1", "IOS", listOf(section)),
            listOf(payload)
        )
        return "file://$document"
    }
}
