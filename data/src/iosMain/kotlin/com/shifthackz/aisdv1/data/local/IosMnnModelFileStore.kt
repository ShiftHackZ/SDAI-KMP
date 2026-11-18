package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.domain.gateway.MnnArchiveExtractor
import com.shifthackz.aisdv1.domain.gateway.MnnModelStore
import com.shifthackz.aisdv1.domain.gateway.MnnBundleFileSystem
import com.shifthackz.aisdv1.domain.validator.MnnBundleValidator
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import platform.Foundation.NSFileManager
import platform.Foundation.NSRecursiveLock
import platform.Foundation.NSUUID

/**
 * Publishes validated catalog bundles atomically and defers deletion until all native readers drain.
 *
 * @param descriptor Durable application-owned model root.
 * @param extractor Strict regular-root ZIP extractor supplied by the Swift composition root.
 * @param validator One shared fixed-profile integrity policy.
 * @param fileSystem Regular-file checks for durable completion markers.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosMnnModelFileStore(
    descriptor: FileProviderDescriptor,
    private val extractor: MnnArchiveExtractor,
    private val validator: MnnBundleValidator,
    private val fileSystem: MnnBundleFileSystem
) : MnnModelStore {
    private val modelRoot = fileSystem.canonicalRoot(descriptor.localModelDirPath).trimEnd('/')
    private val manager = NSFileManager.defaultManager
    private val lock = NSRecursiveLock()
    private val readers = mutableMapOf<String, Int>()
    private val selections = mutableMapOf<String, Int>()

    init {
        if (manager.fileExistsAtPath(modelRoot)) {
            checkNotNull(manager.contentsOfDirectoryAtPath(modelRoot, null))
                .filterIsInstance<String>().forEach { name ->
                    val candidate = "${modelRoot}/$name"
                    if (listOf(
                            ".mnn-staging-",
                            ".mnn-download-",
                            ".mnn-retired-"
                        ).any(name::startsWith) ||
                        (name.matches(Regex("mnn_[A-Za-z0-9_-]+")) &&
                                manager.fileExistsAtPath("$candidate/.delete-requested"))
                    ) remove(candidate)
                }
        }
    }

    override fun path(id: String, activationId: String): String {
        require(id.matches(Regex("mnn_[A-Za-z0-9_-]+")) && id != "mnn_custom") {
            "iOS MNN accepts only catalog model identities"
        }
        return "${modelRoot}/$id"
    }

    override suspend fun importCustom(sourcePath: String): String =
        error("iOS MNN accepts only catalog downloads")

    override fun isDownloaded(id: String): Boolean = locked {
        val root = path(id)
        fileSystem.isRegularFile("$root/.complete") && !manager.fileExistsAtPath("$root/.delete-requested")
    }

    override fun acquire(id: String, activationId: String): AutoCloseable = locked {
        check(isDownloaded(id)) { "MNN model is unavailable" }
        readers[id] = (readers[id] ?: 0) + 1
        lease {
            val remaining = checkNotNull(readers[id]) - 1
            if (remaining == 0) {
                readers.remove(id)
                if (manager.fileExistsAtPath("${path(id)}/.delete-requested")) remove(path(id))
            } else readers[id] = remaining
        }
    }

    override fun acquireSelection(id: String, activationId: String): AutoCloseable = locked {
        check(isDownloaded(id)) { "MNN model is unavailable" }
        selections[id] = (selections[id] ?: 0) + 1
        lease {
            val remaining = checkNotNull(selections[id]) - 1
            if (remaining == 0) selections.remove(id) else selections[id] = remaining
        }
    }

    override fun delete(id: String) = locked {
        check((selections[id] ?: 0) == 0) { "MNN model selection is busy" }
        val root = path(id)
        if (manager.fileExistsAtPath(root)) {
            check(manager.createFileAtPath("$root/.delete-requested", null, null))
            if ((readers[id] ?: 0) == 0) remove(root)
        }
    }

    override suspend fun activate(id: String, archivePath: String) {
        val root = path(id)
        val staging = "${modelRoot}/.mnn-staging-${NSUUID().UUIDString}"
        check(
            manager.createDirectoryAtPath(
                staging,
                true,
                null,
                null
            )
        ) { "MNN staging creation failed" }
        var primaryFailure: Throwable? = null
        try {
            currentCoroutineContext().ensureActive()
            extractor.extract(archivePath, staging)
            validator(staging)
            currentCoroutineContext().ensureActive()
            check(manager.createFileAtPath("$staging/.complete", null, null))
            locked {
                check((readers[id] ?: 0) == 0 && (selections[id] ?: 0) == 0) { "MNN model is busy" }
                check(!isDownloaded(id)) { "MNN model already installed" }
                val retired = "${modelRoot}/.mnn-retired-${NSUUID().UUIDString}"
                val hasPrevious = manager.fileExistsAtPath(root)
                if (hasPrevious) check(manager.moveItemAtPath(root, retired, null))
                if (!manager.moveItemAtPath(staging, root, null)) {
                    if (hasPrevious) check(manager.moveItemAtPath(retired, root, null))
                    error("MNN activation failed")
                }
                if (hasPrevious) remove(retired)
            }
        } catch (failure: Throwable) {
            primaryFailure = failure
        }
        if (manager.fileExistsAtPath(staging)) try {
            remove(staging)
        } catch (cleanup: Throwable) {
            if (primaryFailure == null) primaryFailure = cleanup else primaryFailure.addSuppressed(
                cleanup
            )
        }
        primaryFailure?.let { throw it }
    }

    private fun lease(release: () -> Unit): AutoCloseable = object : AutoCloseable {
        private var isClosed = false
        override fun close() = locked {
            if (!isClosed) {
                isClosed = true
                release()
            }
        }
    }

    private fun remove(path: String) {
        check(manager.removeItemAtPath(path, null)) { "MNN owned storage cleanup failed" }
    }

    private inline fun <T> locked(action: () -> T): T {
        lock.lock()
        try {
            return action()
        } finally {
            lock.unlock()
        }
    }
}
