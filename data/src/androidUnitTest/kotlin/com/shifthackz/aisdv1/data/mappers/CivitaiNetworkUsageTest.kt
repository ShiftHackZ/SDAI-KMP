package com.shifthackz.aisdv1.data.mappers

import com.shifthackz.aisdv1.domain.entity.NetworkUsageBucket
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.storage.db.persistent.entity.NetworkUsageEntity
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Protects the persistent Civitai bucket and backward-compatible totals when older stores lack its row.
 *
 * @author Dmitriy Moroz
 */
class CivitaiNetworkUsageTest {
    @Test
    fun `given persisted Civitai and provider counters, usage is loaded, expected separate buckets included in total`() {
        // Given
        val rows = listOf(
            NetworkUsageEntity("model_downloads", 100),
            NetworkUsageEntity("configs", 20),
            NetworkUsageEntity("inference", 30),
            NetworkUsageEntity("civitai", 40),
            NetworkUsageEntity("future", 999)
        )
        // When
        val usage = rows.toDomain()
        // Then
        assertEquals(40L, usage.civitaiBytes)
        assertEquals(30L, usage.inferenceBytes)
        assertEquals(20L, usage.configBytes)
        assertEquals(190L, usage.totalBytes)
    }

    @Test
    fun `given legacy network rows, usage is loaded, expected zero Civitai traffic without changing prior totals`() {
        // Given
        val rows = listOf(NetworkUsageEntity("configs", 20), NetworkUsageEntity("inference", 30))
        // When
        val usage = rows.toDomain()
        // Then
        assertEquals(0L, usage.civitaiBytes)
        assertEquals(50L, usage.totalBytes)
    }

    @Test
    fun `given invalid negative Civitai counter, usage is loaded, expected nonnegative total`() {
        // Given
        val rows = listOf(NetworkUsageEntity("civitai", -100))
        // When
        val usage = rows.toDomain()
        // Then
        assertEquals(0L, usage.civitaiBytes)
        assertEquals(0L, usage.totalBytes)
    }

    @Test
    fun `given Civitai transport category, counter is persisted, expected stable independently parseable bucket key`() {
        // Given
        val category = NetworkUsageCategory.CIVITAI
        // When
        val bucket = category.toDomain()
        // Then
        assertEquals(NetworkUsageBucket.CIVITAI, bucket)
        assertEquals("civitai", bucket.key)
        assertEquals(bucket, NetworkUsageBucket.parse(bucket.key))
    }
}
