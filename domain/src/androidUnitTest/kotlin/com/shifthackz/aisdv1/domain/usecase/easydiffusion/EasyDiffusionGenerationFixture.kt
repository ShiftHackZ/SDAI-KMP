package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionRequest
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionTask
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionFrame
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.NetworkTimeout
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.gateway.EasyDiffusionTaskRegistry
import com.shifthackz.aisdv1.domain.mocks.mockAiGenerationResult
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.EasyDiffusionRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Supplies deterministic repository boundaries to the real generation policy and real request models.
 * @author Dmitriy Moroz
 */
internal class EasyDiffusionGenerationFixture {
    val repository = mockk<EasyDiffusionRepository>()
    val registry = mockk<EasyDiffusionTaskRegistry>(relaxed = true)
    val preferences = mockk<PreferenceManager>()
    val settings = MutableStateFlow(Settings(source = ServerSource.EASY_DIFFUSION))
    var source = ServerSource.EASY_DIFFUSION
    var revision = 7L
    var url = "https://server.example/prefix"
    val request = EasyDiffusionRequest(
        url, "session", "nested/model", "lake", "blur",
        512, 512, 20, 7f, 4294967295L, 1, selectionRevision = revision
    )
    val task = EasyDiffusionTask(url, request.sessionId, 42)
    val terminal = EasyDiffusionFrame(status = "succeeded", image = "encoded", seed = request.seed)
    val useCase = GenerateEasyDiffusionUseCaseImpl(repository, registry, preferences)

    init {
        every { preferences.source } answers { source }
        every { preferences.providerSelectionRevision } answers { revision }
        every { preferences.easyDiffusionUrl } answers { url }
        every { preferences.generationRequestTimeout } returns NetworkTimeout.TWO_MINUTES
        every { preferences.connectionTimeout } returns NetworkTimeout.TWO_MINUTES
        every { preferences.observe() } returns settings
        coEvery { repository.fetchModels(any()) } returns Unit
        coEvery { repository.getModels(any()) } returns listOf(
            EasyDiffusionModel(
                request.model,
                "Model"
            )
        )
        coEvery { repository.submit(any()) } returns task
        coEvery { repository.readStream(any(), any()) } coAnswers {
            secondArg<suspend (EasyDiffusionFrame) -> Unit>()(terminal)
        }
        coEvery { repository.ping(any(), any()) } returns "Rendering"
        coEvery { repository.save(any(), any()) } returns mockAiGenerationResult
        coEvery { repository.stop(any()) } returns Unit
    }
}
