@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.storage.gateway

import androidx.room.useReaderConnection
import cnames.structs.sqlite3
import cnames.structs.sqlite3_stmt
import com.shifthackz.aisdv1.storage.db.persistent.PersistentDatabase
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import sqlite3.SQLITE_DONE
import sqlite3.SQLITE_OK
import sqlite3.SQLITE_OPEN_READONLY
import sqlite3.SQLITE_OPEN_FULLMUTEX
import sqlite3.SQLITE_ROW
import sqlite3.SQLITE_TRANSIENT
import sqlite3.sqlite3_bind_int
import sqlite3.sqlite3_bind_int64
import sqlite3.sqlite3_bind_text
import sqlite3.sqlite3_busy_timeout
import sqlite3.sqlite3_close_v2
import sqlite3.sqlite3_column_blob
import sqlite3.sqlite3_column_bytes
import sqlite3.sqlite3_finalize
import sqlite3.sqlite3_open_v2
import sqlite3.sqlite3_prepare_v2
import sqlite3.sqlite3_step
import platform.posix.memcpy

/**
 * Bulk-copies large image payloads instead of the native driver's per-byte Kotlin conversion loop.
 * Room remains the schema/migration owner; this reader uses its bundled SQLite C API to open the same WAL database read-only.
 * Each operation owns and closes its native connection and statement without caching original images.
 *
 * @param database Room owner initialized before opening a native read snapshot.
 * @param databasePath Identical persistent database path supplied to the Room builder.
 * @author Dmitriy Moroz
 */
internal class NativeGalleryImagePayloadReader(
    private val database: PersistentDatabase,
    private val databasePath: String,
) : GalleryImagePayloadReader {
    /**
     * Ensures Room initialization, then reads one revision- and size-matched payload off the UI thread.
     *
     * @param id Saved image identity.
     * @param revision Expected content revision.
     * @param isInput Original input or result selection.
     * @param maxChars Maximum admitted Base64 characters/ASCII bytes.
     * @author Dmitriy Moroz
     */
    override suspend fun read(
        id: Long,
        revision: String,
        isInput: Boolean,
        maxChars: Int
    ): ByteArray? =
        database.useReaderConnection {
            withContext(Dispatchers.Default) {
                require(maxChars > 0)
                currentCoroutineContext().ensureActive()
                readPayload(id, revision, isInput, maxChars).also {
                    currentCoroutineContext().ensureActive()
                }
            }
        }

    /**
     * Copies the admitted SQLite BLOB directly into pinned Kotlin storage before releasing its statement.
     *
     * @param id Saved image identity.
     * @param revision Expected content revision.
     * @param isInput Original input or result selection.
     * @param maxChars Maximum admitted Base64 characters/ASCII bytes.
     * @author Dmitriy Moroz
     */
    private fun readPayload(
        id: Long,
        revision: String,
        isInput: Boolean,
        maxChars: Int
    ): ByteArray? = memScoped {
        val connection = alloc<CPointerVar<sqlite3>>()
        connection.value = null
        try {
            checkResult(
                sqlite3_open_v2(
                    databasePath,
                    connection.ptr,
                    SQLITE_OPEN_READONLY or SQLITE_OPEN_FULLMUTEX,
                    null
                )
            )
            val db = requireNotNull(connection.value)
            checkResult(sqlite3_busy_timeout(db, 1000))
            val column = if (isInput) "original_image_base_64" else "image_base_64"
            val sql =
                "SELECT CAST($column AS BLOB) FROM generation_results WHERE id = ? AND image_revision = ? AND length($column) <= ? LIMIT 1"
            val statement = alloc<CPointerVar<sqlite3_stmt>>()
            statement.value = null
            try {
                checkResult(
                    sqlite3_prepare_v2(
                        db,
                        sql.cstr.getPointer(this),
                        -1,
                        statement.ptr,
                        null
                    )
                )
                val query = requireNotNull(statement.value)
                checkResult(sqlite3_bind_int64(query, 1, id))
                checkResult(sqlite3_bind_text(query, 2, revision, -1, SQLITE_TRANSIENT))
                checkResult(sqlite3_bind_int(query, 3, maxChars))
                when (val status = sqlite3_step(query)) {
                    SQLITE_DONE -> null
                    SQLITE_ROW -> copyPayload(query, maxChars)
                    else -> error("Gallery SQLite query failed ($status)")
                }
            } finally {
                statement.value?.let { sqlite3_finalize(it) }
            }
        } finally {
            connection.value?.let { sqlite3_close_v2(it) }
        }
    }

    /**
     * Copies only a positive admitted payload; empty input remains an empty encoded image.
     *
     * @param statement Stepped native statement still owned by the caller.
     * @param maxChars Maximum encoded ASCII bytes allowed in Kotlin memory.
     * @author Dmitriy Moroz
     */
    private fun copyPayload(statement: CPointer<sqlite3_stmt>, maxChars: Int): ByteArray {
        val size = sqlite3_column_bytes(statement, 0)
        check(size in 0..maxChars) { "Gallery image exceeds encoded byte budget" }
        if (size == 0) return ByteArray(0)
        val source = requireNotNull(sqlite3_column_blob(statement, 0))
        return ByteArray(size).also { bytes ->
            bytes.usePinned { memcpy(it.addressOf(0), source, size.toULong()) }
        }
    }

    /**
     * Propagates native setup/binding errors instead of returning an unavailable-image success.
     *
     * @param status SQLite return code from an operation that must succeed.
     * @author Dmitriy Moroz
     */
    private fun checkResult(status: Int) {
        check(status == SQLITE_OK) { "Gallery SQLite operation failed ($status)" }
    }
}
