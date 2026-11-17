package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.domain.gateway.MnnBundleFileSystem
import com.shifthackz.aisdv1.domain.entity.mnn.MnnFileMetadata
import java.io.File
import java.security.MessageDigest

/**
 * Provides Android regular-file and streaming SHA256 operations for the shared bundle policy.
 *
 * @author Dmitriy Moroz
 */
internal class AndroidMnnBundleFileSystem : MnnBundleFileSystem {
    override fun canonicalRoot(path: String): String = File(path).canonicalPath
    override fun readManifest(path: String): String {
        check(isRegularFile(path) && metadata(path).sizeBytes in 1..65_536)
        return File(path).readText()
    }

    override fun sha256(path: String): String {
        check(isRegularFile(path))
        val hash = MessageDigest.getInstance("SHA-256")
        File(path).inputStream().buffered().use { input ->
            val buffer = ByteArray(65_536)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                hash.update(buffer, 0, count)
            }
        }
        return hash.digest().joinToString("") { "%02x".format(it) }
    }

    override fun isRegularFile(path: String): Boolean = File(path).let {
        it.isFile && it.canonicalFile == it.absoluteFile
    }

    override fun metadata(path: String): MnnFileMetadata = File(path).let {
        check(isRegularFile(path))
        MnnFileMetadata(it.length())
    }

    override fun entries(path: String): List<String> =
        checkNotNull(File(path).list()) { "MNN bundle listing failed" }.toList()
}
