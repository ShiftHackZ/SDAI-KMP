package com.shifthackz.aisdv1.feature.backup.archive

import okio.use
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.time.TimeProvider
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.NetworkUsageBucket
import com.shifthackz.aisdv1.domain.entity.setupAllowedModes
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupEntry
import com.shifthackz.aisdv1.domain.entity.backup.BackupExport
import com.shifthackz.aisdv1.domain.entity.backup.BackupInspection
import com.shifthackz.aisdv1.domain.entity.backup.BackupPreferences
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.domain.entity.backup.BackupValue
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.gateway.backup.BackupPreferencesGateway
import com.shifthackz.aisdv1.feature.backup.codec.BackupArchiveCodec
import com.shifthackz.aisdv1.feature.backup.codec.BackupManifest
import com.shifthackz.aisdv1.feature.backup.codec.BackupSection
import com.shifthackz.aisdv1.feature.backup.codec.BackupPreferencesCodec
import com.shifthackz.aisdv1.feature.backup.codec.GalleryBackupCodec
import com.shifthackz.aisdv1.feature.backup.io.BackupFileStore
import com.shifthackz.aisdv1.storage.gateway.BackupStorage
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.Buffer
import okio.FileSystem
import okio.Path
import okio.buffer

/**
 * Creates consistent paged snapshots and validates immutable staged files before restore.
 * @param files Platform document transport and durable private directory.
 * @param codec Bounded binary framing and section integrity.
 * @param galleryCodec Portable gallery record codec.
 * @param preferencesCodec Portable scalar field codec.
 * @param database Shared Room database used for consistent snapshot reads.
 * @param preferencesGateway Strict allowlisted preference store.
 * @param authorizationStore Protected portable Basic Auth source.
 * @param selectionGuard Serializes provider snapshot reads with configuration publication.
 * @param buildInfo Flavor/platform availability and source version.
 * @param clock UTC archive timestamp.
 * @param json Forward-compatible metadata serializer.
 * @param staging Admits file creation only after one-time orphan cleanup has preserved pending restores.
 * @author Dmitriy Moroz
 */
internal class DefaultBackupArchiveStore(
    private val files: BackupFileStore,
    private val codec: BackupArchiveCodec,
    private val galleryCodec: GalleryBackupCodec,
    private val preferencesCodec: BackupPreferencesCodec,
    private val database: BackupStorage,
    private val preferencesGateway: BackupPreferencesGateway,
    private val authorizationStore: AuthorizationStore,
    private val selectionGuard: ProviderSelectionGuard,
    private val buildInfo: BuildInfoProvider,
    private val clock: TimeProvider,
    private val json: Json,
    private val staging: BackupStagingCleaner,
) : BackupArchiveStore {
    override fun preparedExport(handle: String, filename: String): BackupExport {
        val path = directory(handle) / ARCHIVE
        check(FileSystem.SYSTEM.exists(path)) { "Pending backup source is unavailable" }
        return BackupExport(files.documentReference(path), filename, handle)
    }

    override suspend fun inventory(): BackupInspection {
        val preferences = ordinarySnapshot()
        return BackupInspection(
            "",
            0,
            "",
            "",
            listOf(
                BackupEntry(
                    BackupCategory.PROVIDERS,
                    preferences.providers.size.toLong(),
                    preferences.providers.isNotEmpty()
                ),
                BackupEntry(
                    BackupCategory.GALLERY,
                    database.generationResultDao().observeCount().first().toLong()
                ),
                BackupEntry(
                    BackupCategory.NETWORK,
                    database.networkUsageDao().query().size.toLong()
                ),
                BackupEntry(
                    BackupCategory.SETTINGS,
                    preferences.settings.size.toLong(),
                    preferences.settings.isNotEmpty()
                ),
            ).map { if (it.itemCount == 0L) it.copy(isAvailable = false) else it },
            preferences.providers.keys
        )
    }

    override suspend fun create(
        selection: BackupSelection,
        progress: (BackupProgress) -> Unit
    ): BackupExport {
        staging.prepare()
        require(selection.categories.isNotEmpty()) { "Select backup data" }
        val snapshot =
            if (BackupCategory.PROVIDERS in selection.categories) portableSnapshot() else ordinarySnapshot()
        val handle = files.newId()
        val directory = directory(handle)
        FileSystem.SYSTEM.createDirectories(directory)
        try {
            val sections = mutableListOf<BackupSection>()
            val payloads = mutableListOf<Path>()
            suspend fun payload(category: BackupCategory, block: suspend (Path) -> Long) {
                if (category !in selection.categories) return
                currentCoroutineContext().ensureActive()
                val path = directory / category.key
                val count = block(path)
                sections += codec.describe(path, category.key, count)
                payloads += path
            }
            payload(BackupCategory.PROVIDERS) { path ->
                val values = snapshot.providers.filterKeys { it in selection.providers }
                require(values.isNotEmpty()) { "Select providers" }
                FileSystem.SYSTEM.sink(path).buffer()
                    .use { codec.writeJson(it, preferencesCodec.encodeProviders(values)) }
                values.size.toLong()
            }
            payload(BackupCategory.SETTINGS) { path ->
                FileSystem.SYSTEM.sink(path).buffer()
                    .use { codec.writeJson(it, preferencesCodec.encode(snapshot.settings)) }
                snapshot.settings.size.toLong()
            }
            database.readSnapshot {
                payload(BackupCategory.GALLERY) { path ->
                    var offset = 0L
                    var afterId = 0L
                    FileSystem.SYSTEM.sink(path).buffer().use { sink ->
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val row = database.generationResultDao().queryBackupPage(afterId, 1)
                                .firstOrNull() ?: break
                            galleryCodec.write(sink, row)
                            offset++
                            afterId = row.id
                            progress(BackupProgress(BackupCategory.GALLERY, offset.toLong(), 0))
                        }
                    }
                    offset.toLong()
                }
                payload(BackupCategory.NETWORK) { path ->
                    val values = database.networkUsageDao().query()
                        .filter { NetworkUsageBucket.parse(it.category) != null }
                        .associate { it.category to it.bytes }
                    require(values.values.all { it >= 0 }) { "Invalid saved network counter" }
                    FileSystem.SYSTEM.sink(path).buffer()
                        .use { codec.writeJson(it, json.encodeToString(values)) }
                    values.size.toLong()
                }
            }
            val manifest = BackupManifest(
                handle,
                clock.currentTimeMillis(),
                buildInfo.version.toString(),
                buildInfo.platform.name,
                sections
            )
            codec.write(directory / ARCHIVE, manifest, payloads)
            payloads.forEach { FileSystem.SYSTEM.delete(it) }
            return BackupExport(
                files.documentReference(directory / ARCHIVE),
                "SDAI-${manifest.createdAt}.sdaibackup",
                handle
            )
        } catch (failure: Throwable) {
            FileSystem.SYSTEM.deleteRecursively(directory, mustExist = false)
            throw failure
        }
    }

    override suspend fun inspect(
        document: String,
        progress: (BackupProgress) -> Unit
    ): BackupInspection {
        staging.prepare()
        val handle = files.newId()
        val directory = directory(handle)
        FileSystem.SYSTEM.createDirectories(directory)
        try {
            val target = directory / ARCHIVE
            val displayName = files.documentName(document).take(512)
            FileSystem.SYSTEM.sink(directory / "name").buffer().use { it.writeUtf8(displayName) }
            val sourceLocation = files.documentLocation(document).take(2048)
            FileSystem.SYSTEM.sink(directory / "location").buffer()
                .use { it.writeUtf8(sourceLocation) }
            files.openDocument(document).use { source ->
                FileSystem.SYSTEM.sink(target).buffer().use { sink ->
                    val buffer = Buffer()
                    var copied = 0L
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read = source.read(buffer, 65536)
                        if (read == -1L) break
                        require(files.availableBytes() > read + 1024 * 1024) { "Not enough storage for backup" }
                        sink.write(buffer, read)
                        copied += read
                        progress(BackupProgress(null, copied, 0))
                    }
                }
            }
            val archive = staged(handle)
            validatePayloads(archive, progress)
            return summary(archive)
        } catch (failure: Throwable) {
            FileSystem.SYSTEM.deleteRecursively(directory, mustExist = false)
            throw failure
        }
    }

    override suspend fun staged(handle: String): StagedBackup {
        val path = directory(handle) / ARCHIVE
        return StagedBackup(handle, path, codec.inspect(path))
    }

    override fun preferences(archive: StagedBackup): BackupPreferences {
        val providers = if (compatible(archive, BackupCategory.PROVIDERS)) {
            codec.openSection(archive.path, archive.manifest, BackupCategory.PROVIDERS.key)
                .use { source ->
                    preferencesCodec.decodeProviders(codec.readJson(source))
                        .also { require(source.exhausted()) { "Unexpected provider payload" } }
                }
        } else emptyMap()
        val settings = if (compatible(archive, BackupCategory.SETTINGS)) {
            codec.openSection(archive.path, archive.manifest, BackupCategory.SETTINGS.key)
                .use { source ->
                    preferencesCodec.decode(codec.readJson(source))
                        .also { require(source.exhausted()) { "Unexpected settings payload" } }
                }
        } else emptyMap()
        val allowed = buildInfo.setupAllowedModes().map { it.key }.toSet()
        val supported = preferencesGateway.filterSupported(
            BackupPreferences(
                providers.filterKeys { it in allowed },
                settings
            )
        )
        val withCredentials = supported.providers.mapValues { (key, values) ->
            val basic =
                providers[key].orEmpty()
                    .filter { (field, value) -> key in BASIC_PROVIDERS && field in BASIC_FIELDS && value is BackupValue.Text }
            values + basic
        }
        return supported.copy(providers = withCredentials.filterValues { it.isNotEmpty() })
    }

    override fun summary(archive: StagedBackup): BackupInspection {
        val preferences = preferences(archive)
        val entries = archive.manifest.sections.mapNotNull { section ->
            val category = BackupCategory.entries.firstOrNull { it.key == section.id }
                ?: return@mapNotNull null
            val count = when (category) {
                BackupCategory.PROVIDERS -> preferences.providers.size.toLong()
                BackupCategory.SETTINGS -> preferences.settings.size.toLong()
                else -> section.itemCount
            }
            BackupEntry(
                category,
                count,
                section.codec == "stored" && section.requiredReaderVersion <= 1 && count > 0
            )
        }
        var skipped =
            archive.manifest.sections.count { section -> entries.none { it.category.key == section.id && it.isAvailable } }
        if (compatible(archive, BackupCategory.PROVIDERS)) {
            val original = codec.openSection(archive.path, archive.manifest, "providers")
                .use { preferencesCodec.decodeProviders(codec.readJson(it)) }
            skipped += original.entries.sumOf { (provider, fields) ->
                val supported = preferences.providers[provider]
                if (supported == null) 1 else fields.keys.count { it !in supported }
            }
        }
        if (compatible(archive, BackupCategory.SETTINGS)) {
            val original = codec.openSection(archive.path, archive.manifest, "settings")
                .use { preferencesCodec.decode(codec.readJson(it)) }
            skipped += original.keys.count { it !in preferences.settings }
        }
        val nameFile = archive.path.parent!! / "name"
        val filename =
            if (FileSystem.SYSTEM.exists(nameFile)) FileSystem.SYSTEM.source(nameFile).buffer()
                .use { it.readUtf8() } else "SDAI.sdaibackup"
        return BackupInspection(
            archive.handle,
            archive.manifest.createdAt,
            archive.manifest.appVersion,
            archive.manifest.sourcePlatform,
            entries,
            preferences.providers.keys,
            skipped,
            filename,
            sourceLocation = (archive.path.parent!! / "location").let { location ->
                if (FileSystem.SYSTEM.exists(location)) FileSystem.SYSTEM.source(location).buffer()
                    .use { it.readUtf8() } else ""
            },
        )
    }

    override suspend fun export(document: String, destination: String) {
        val path = privateDocument(document)
        FileSystem.SYSTEM.source(path).buffer().use { source ->
            files.writeDocument(destination).use { sink ->
                val buffer = Buffer()
                while (true) {
                    currentCoroutineContext().ensureActive()
                    val count = source.read(buffer, 65536)
                    if (count == -1L) break
                    sink.write(buffer, count)
                }
            }
        }
    }

    override fun discard(handle: String) {
        val id = if (handle.startsWith("file://")) privateDocument(handle).parent!!.name else handle
        // The discard use case resolves pending checked writes and abandons intent before cleanup.
        FileSystem.SYSTEM.deleteRecursively(directory(id), mustExist = false)
    }

    private fun ordinarySnapshot(): BackupPreferences {
        val snapshot = preferencesGateway.read()
        val allowed = buildInfo.setupAllowedModes().map { it.key }.toSet()
        return snapshot.copy(providers = snapshot.providers.filterKeys { it in allowed })
    }

    private suspend fun portableSnapshot(): BackupPreferences = selectionGuard.withSelection {
        val snapshot = preferencesGateway.read()
        val allowed = buildInfo.setupAllowedModes().map { it.key }.toSet()
        val providers = snapshot.providers.filterKeys { it in allowed }.toMutableMap()
        val credentials = authorizationStore.getAuthorizationCredentials()
        if (credentials is AuthorizationCredentials.HttpBasic) {
            BASIC_PROVIDERS.filter { it in allowed }.forEach { key ->
                providers[key] = providers[key].orEmpty() + mapOf(
                    "basicLogin" to BackupValue.Text(credentials.login),
                    "basicPassword" to BackupValue.Text(credentials.password)
                )
            }
        }
        snapshot.copy(providers = providers)
    }

    private suspend fun validatePayloads(
        archive: StagedBackup,
        progress: (BackupProgress) -> Unit
    ) {
        preferences(archive)
        for (section in archive.manifest.sections) {
            if (section.codec != "stored" || section.requiredReaderVersion > 1) continue
            when (section.id) {
                "gallery" -> codec.openSection(archive.path, archive.manifest, section.id)
                    .use { source ->
                        repeatCount(section.itemCount) { index ->
                            currentCoroutineContext().ensureActive()
                            galleryCodec.read(source, "validation")
                            progress(
                                BackupProgress(
                                    BackupCategory.GALLERY,
                                    index + 1,
                                    section.itemCount
                                )
                            )
                        }
                        require(source.exhausted()) { "Unexpected gallery payload" }
                    }

                "network" -> codec.openSection(archive.path, archive.manifest, section.id)
                    .use { source ->
                        val counters =
                            json.decodeFromString<Map<String, Long>>(codec.readJson(source))
                        require(counters.values.all { it >= 0 } && source.exhausted()) { "Invalid network payload" }
                    }
            }
        }
    }

    private fun compatible(archive: StagedBackup, category: BackupCategory) =
        archive.manifest.sections.any {
            it.id == category.key && it.codec == "stored" && it.requiredReaderVersion <= 1
        }

    private fun directory(handle: String): Path {
        require(handle.matches(Regex("[0-9a-fA-F-]{36}"))) { "Invalid backup handle" }
        return files.root / handle
    }

    private fun privateDocument(document: String): Path {
        val path = files.documentPath(document)
        val parts = path.relativeTo(files.root).segments
        require(parts.size == 2 && parts[1] == ARCHIVE) { "Invalid backup document" }
        return directory(parts[0]) / ARCHIVE
    }

    private suspend fun repeatCount(count: Long, block: suspend (Long) -> Unit) {
        var index = 0L
        while (index < count) {
            block(index); index++
        }
    }

    private companion object {
        const val ARCHIVE = "archive.sdaibackup"
        val BASIC_PROVIDERS = setOf("custom", "swarm_ui")
        val BASIC_FIELDS = setOf("basicLogin", "basicPassword")
    }
}
