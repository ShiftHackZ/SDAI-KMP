package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionConnection
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.EasyDiffusionRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.verify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Rejects stale queued requests and picker events using the production provider-selection lock.
 * @author Dmitriy Moroz
 */
class EasyDiffusionSelectionTest {
    private val preferences = mockk<PreferenceManager>(relaxed = true)
    private val repository = mockk<EasyDiffusionRepository>()
    private val guard = ProviderSelectionGuard()
    private val capture = CaptureEasyDiffusionConnectionUseCaseImpl(preferences, guard)
    private val select = SelectEasyDiffusionModelUseCaseImpl(preferences, repository, guard)
    private val url = "https://server.example"

    init {
        every { preferences.source } returns ServerSource.EASY_DIFFUSION
        every { preferences.providerSelectionRevision } returns 7
        every { preferences.easyDiffusionUrl } returns url
        every { preferences.easyDiffusionModel } returns "folder/model"
        coEvery { repository.getModels(url) } returns listOf(
            EasyDiffusionModel(
                "folder/model",
                "Model"
            )
        )
    }

    @Test
    fun `capture preserves current identity and accepts legacy unspecified revision`() = runTest {
        assertEquals(EasyDiffusionConnection(url, "folder/model", 7), capture(7, url))
        assertEquals(capture(7, url), capture(null, null))
    }

    @Test
    fun `capture rejects stale revision endpoint and provider`() = runTest {
        assertTrue(runCatching { capture(6, url) }.isFailure)
        assertTrue(runCatching { capture(7, "https://old.example") }.isFailure)
        every { preferences.source } returns ServerSource.AUTOMATIC1111
        assertTrue(runCatching { capture(7, url) }.isFailure)
    }

    @Test
    fun `picker rejects stale or absent models without writing preferences`() = runTest {
        assertFalse(select("folder/model", url, 6))
        assertFalse(select("folder/model", "https://old.example", 7))
        assertFalse(select("missing", url, 7))
        every { preferences.source } returns ServerSource.AUTOMATIC1111
        assertFalse(select("folder/model", url, 7))
        verify(exactly = 0) { preferences.easyDiffusionModel = any() }
    }

    @Test
    fun `picker retains complete catalog model identifier`() = runTest {
        assertTrue(select("folder/model", url, 7))
        verify(exactly = 1) { preferences.easyDiffusionModel = "folder/model" }
    }
}
