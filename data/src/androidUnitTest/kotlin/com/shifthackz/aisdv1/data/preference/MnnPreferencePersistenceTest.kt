package com.shifthackz.aisdv1.data.preference

import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Uses the production preference readers and durable batch writer against a strict in-memory boundary.
 *
 * @author Dmitriy Moroz
 */
class MnnPreferencePersistenceTest {
    private val values = mutableMapOf<String, String>()
    private val booleans = mutableMapOf<String, Boolean>()
    private val keyValues = mockk<KeyValueStore>()

    @Before
    fun initialize() {
        every { keyValues.getString(any(), any()) } answers { values[firstArg()] ?: secondArg() }
        every { keyValues.putString(any(), any()) } answers { values[firstArg()] = secondArg() }
        every { keyValues.putStringDurably(any(), any()) } answers {
            values[firstArg()] = secondArg()
        }
        every { keyValues.getBoolean(any(), any()) } answers { booleans[firstArg()] ?: secondArg() }
        every { keyValues.putConfigurationDurably(any(), any()) } answers {
            values.putAll(firstArg<Map<String, String>>())
            booleans.putAll(secondArg<Map<String, Boolean>>())
        }
    }

    @Test
    fun `given MNN configuration adapter, unknown raw backend is restored, expected CPU persisted before snapshot`() {
        // Given
        values["key_mnn_backend"] = "future_backend"
        val store = KeyValueConfigurationStore(keyValues)

        // When
        val backend = store.localMnnBackend

        // Then
        assertEquals(MnnBackend.CPU, backend)
        assertEquals("cpu", values["key_mnn_backend"])
    }

    @Test
    fun `given MNN preference adapter, unknown raw backend is restored, expected CPU persisted before capture`() {
        // Given
        values["key_mnn_backend"] = "future_backend"
        val preferences = PreferenceManagerImpl(keyValues)

        // When
        val backend = preferences.localMnnBackend

        // Then
        assertEquals(MnnBackend.CPU, backend)
        assertEquals("cpu", values["key_mnn_backend"])
    }

    @Test
    fun `given MNN preferences, no values exist, expected independent CPU and empty identities`() {
        // Given
        val preferences = PreferenceManagerImpl(keyValues)

        // When
        val state = listOf(
            preferences.localMnnModelId,
            preferences.localMnnActivationId,
            preferences.localMnnCustomActivationId
        )

        // Then
        assertEquals(listOf("", "", ""), state)
        assertEquals(MnnBackend.CPU, preferences.localMnnBackend)
    }

    @Test
    fun `given MNN preferences, fields change and reader restarts, expected complete independent persistence`() {
        // Given
        val preferences = PreferenceManagerImpl(keyValues)
        preferences.localMnnModelId = "mnn_model"
        preferences.localMnnActivationId = "a".repeat(64)
        preferences.localMnnCustomActivationId = "b".repeat(64)
        preferences.localMnnModelPath = "/Downloads/source"
        preferences.localMnnBackend = MnnBackend.OPENCL

        // When
        val restored = PreferenceManagerImpl(keyValues)

        // Then
        assertEquals("mnn_model", restored.localMnnModelId)
        assertEquals("a".repeat(64), restored.localMnnActivationId)
        assertEquals("b".repeat(64), restored.localMnnCustomActivationId)
        assertEquals("/Downloads/source", restored.localMnnModelPath)
        assertEquals(MnnBackend.OPENCL, restored.localMnnBackend)
    }

    @Test
    fun `given MNN configuration, complete durable batch is committed, expected shared readers retain all MNN fields`() {
        // Given
        val configuration = Configuration(
            source = ServerSource.LOCAL_MNN,
            localMnnModelId = "mnn_model",
            localMnnActivationId = "a".repeat(64),
            localMnnCustomActivationId = "b".repeat(64),
            localMnnModelPath = "/Downloads/source",
            localMnnBackend = MnnBackend.OPENCL
        )
        val store = KeyValueConfigurationStore(keyValues)

        // When
        store.commitConfiguration(configuration, "authorization-marker")
        val preferences = PreferenceManagerImpl(keyValues)

        // Then
        assertEquals(configuration.localMnnModelId, preferences.localMnnModelId)
        assertEquals(configuration.localMnnActivationId, preferences.localMnnActivationId)
        assertEquals(
            configuration.localMnnCustomActivationId,
            preferences.localMnnCustomActivationId
        )
        assertEquals(configuration.localMnnModelPath, preferences.localMnnModelPath)
        assertEquals(configuration.localMnnBackend, preferences.localMnnBackend)
        assertEquals(configuration.source, store.source)
        assertEquals("authorization-marker", store.getAuthorizationCommitId())
    }
}
