package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.LocalModelCatalogResult
import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Checks compatibility list operations retain shared strict catalog policy and MNN observation semantics.
 *
 * @author Dmitriy Moroz
 */
class LocalCatalogCompatibilityUseCasesTest {
    private val refresh = mockk<RefreshLocalModelCatalogUseCase>()

    @Test
    fun `given legacy local catalog operations, every provider is requested, expected matching shared refresh type`() =
        runTest {
            // Given
            val expected = LocalAiModel.Type.entries.filter { it != LocalAiModel.Type.Upscaler }
                .associateWith { type ->
                    listOf(
                        LocalAiModel(
                            "${type.key}-model",
                            type,
                            "Model",
                            "1 GB",
                            emptyList()
                        )
                    )
                }
            expected.forEach { (type, models) ->
                coEvery { refresh(type) } returns LocalModelCatalogResult(
                    models
                )
            }

            // When
            val actual = mapOf(
                LocalAiModel.Type.ONNX to GetLocalOnnxModelsUseCaseImpl(refresh)(),
                LocalAiModel.Type.MediaPipe to GetLocalMediaPipeModelsUseCaseImpl(refresh)(),
                LocalAiModel.Type.Sdxl to GetLocalSdxlModelsUseCaseImpl(refresh)(),
                LocalAiModel.Type.CoreMl to GetLocalCoreMlModelsUseCaseImpl(refresh)(),
                LocalAiModel.Type.Bonsai to GetLocalBonsaiModelsUseCaseImpl(refresh)(),
                LocalAiModel.Type.Mnn to GetLocalMnnModelsUseCaseImpl(refresh)().models
            )

            // Then
            assertEquals(expected, actual)
            expected.keys.forEach { type -> coVerify(exactly = 1) { refresh(type) } }
        }

    @Test
    fun `given MNN catalog compatibility operation, refresh retains network failure, expected same visible failure`() =
        runTest {
            // Given
            val failure = IllegalStateException("offline")
            coEvery { refresh(LocalAiModel.Type.Mnn) } returns LocalModelCatalogResult(
                listOf(
                    LocalAiModel.CustomMnn
                ), failure
            )

            // When
            val result = GetLocalMnnModelsUseCaseImpl(refresh)()

            // Then
            assertSame(failure, result.failure)
            assertEquals(listOf(LocalAiModel.CustomMnn), result.models)
        }

    @Test
    fun `given MNN observation, identical snapshots repeat then selection changes, expected only meaningful states`() =
        runTest {
            // Given
            val repository = mockk<DownloadableModelRepository>()
            val initial = listOf(LocalAiModel.CustomMnn)
            val selected = listOf(LocalAiModel.CustomMnn.copy(selected = true))
            every { repository.observeAllMnn() } returns flowOf(initial, initial, selected)

            // When
            val states = ObserveLocalMnnModelsUseCaseImpl(repository)().toList()

            // Then
            assertEquals(listOf(initial, selected), states)
        }
}
