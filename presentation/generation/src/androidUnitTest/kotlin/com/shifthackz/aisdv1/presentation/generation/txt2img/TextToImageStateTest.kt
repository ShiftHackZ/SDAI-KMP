package com.shifthackz.aisdv1.presentation.generation.txt2img

import com.shifthackz.aisdv1.core.validation.ValidationResult
import com.shifthackz.aisdv1.core.validation.dimension.DimensionValidator
import com.shifthackz.aisdv1.domain.entity.ADetailerConfig
import com.shifthackz.aisdv1.domain.entity.BonsaiBackend
import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.domain.entity.HiresConfig
import com.shifthackz.aisdv1.domain.entity.Scheduler
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import org.junit.Assert
import org.junit.Test


class TextToImageStateTest {

    private val dimensionValidator = DimensionValidator { input ->
        when {
            input.isNullOrEmpty() -> ValidationResult(
                isValid = false,
                validationError = DimensionValidator.Error.Empty,
            )

            input.toIntOrNull() == null -> ValidationResult(
                isValid = false,
                validationError = DimensionValidator.Error.Unexpected,
            )

            input.toInt() < MIN_SIZE -> ValidationResult(
                isValid = false,
                validationError = DimensionValidator.Error.LessThanMinimum(MIN_SIZE),
            )

            input.toInt() > MAX_SIZE -> ValidationResult(
                isValid = false,
                validationError = DimensionValidator.Error.BiggerThanMaximum(MAX_SIZE),
            )

            else -> ValidationResult(isValid = true)
        }
    }

    @Test
    fun `given text to image state, A1111 state with hires and ADetailer, expected payload contains configs`() {
        // Given

        val hires = HiresConfig(
            enabled = true,
            upscaler = "Latent",
            scale = 1.5f,
            steps = 12,
            denoisingStrength = 0.35f,
        )
        val aDetailer = ADetailerConfig(
            enabled = true,
            model = "hand_yolov8n.pt",
            confidence = 0.7f,
            denoisingStrength = 0.45f,
        )
        val forgeModules = listOf(
            ForgeModule(
                name = "sdxl_vae",
                path = "/models/VAE/sdxl_vae.safetensors",
            ),
        )

        // When
        val payload = TextToImageState(
            mode = ServerSource.AUTOMATIC1111,
            hires = hires,
            aDetailer = aDetailer,
            aDetailerAvailable = true,
            selectedScheduler = Scheduler.KARRAS,
            selectedForgeModules = forgeModules,
        ).mapToPayload()

        // Then
        Assert.assertEquals(hires, payload.hires)
        Assert.assertEquals(aDetailer, payload.aDetailer)
        Assert.assertEquals(Scheduler.KARRAS, payload.scheduler)
        Assert.assertEquals(forgeModules, payload.forgeModules)

    }

    @Test
    fun `given text to image state, non A1111 state with hires and ADetailer, expected payload disables configs`() {
        // Given
        // The constructor arguments below specify the provider and generation options being mapped;
        // production mapToPayload() is the action.

        // When
        val payload = TextToImageState(
            mode = ServerSource.SWARM_UI,
            hires = HiresConfig(enabled = true),
            aDetailer = ADetailerConfig(enabled = true),
            selectedForgeModules = listOf(ForgeModule(name = "module", path = "/module")),
        ).mapToPayload()

        // Then
        Assert.assertEquals(HiresConfig.DISABLED, payload.hires)
        Assert.assertEquals(ADetailerConfig.DISABLED, payload.aDetailer)
        Assert.assertEquals(Scheduler.AUTOMATIC, payload.scheduler)
        Assert.assertEquals(emptyList<ForgeModule>(), payload.forgeModules)

    }

    @Test
    fun `given text to image state, A1111 state with unavailable ADetailer, expected payload disables ADetailer`() {
        // Given
        // The constructor arguments below specify the provider and generation options being mapped;
        // production mapToPayload() is the action.

        // When
        val payload = TextToImageState(
            mode = ServerSource.AUTOMATIC1111,
            aDetailer = ADetailerConfig(enabled = true),
            aDetailerAvailable = false,
        ).mapToPayload()

        // Then
        Assert.assertEquals(ADetailerConfig.DISABLED, payload.aDetailer)

    }

    @Test
    fun `given text to image state, SDXL state with Vulkan backend, expected payload preserves backend`() {
        // Given
        // The constructor arguments below specify the provider and generation options being mapped;
        // production mapToPayload() is the action.

        // When
        val payload = TextToImageState(
            mode = ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
            sdxlBackend = SdxlBackend.VULKAN,
        ).mapToPayload()

        // Then
        Assert.assertEquals(SdxlBackend.VULKAN, payload.sdxlBackend)

    }

    @Test
    fun `given text to image state, non SDXL state with Vulkan backend, expected payload uses Auto backend`() {
        // Given
        // The constructor arguments below specify the provider and generation options being mapped;
        // production mapToPayload() is the action.

        // When
        val payload = TextToImageState(
            mode = ServerSource.AUTOMATIC1111,
            sdxlBackend = SdxlBackend.VULKAN,
        ).mapToPayload()

        // Then
        Assert.assertEquals(SdxlBackend.AUTO, payload.sdxlBackend)

    }

    @Test
    fun `given text to image state, Bonsai state, expected payload preserves configurable params`() {
        // Given
        // The constructor arguments below specify the provider and generation options being mapped;
        // production mapToPayload() is the action.

        // When
        val payload = TextToImageState(
            mode = ServerSource.LOCAL_APPLE_BONSAI,
            samplingSteps = 30,
            cfgScale = 7.5f,
            batchCount = 4,
            nsfw = true,
            bonsaiBackend = BonsaiBackend.VULKAN,
        ).mapToPayload()

        // Then
        Assert.assertEquals(30, payload.samplingSteps)
        Assert.assertEquals(7.5f, payload.cfgScale)
        Assert.assertEquals(DEFAULT_SIZE, payload.width)
        Assert.assertEquals(DEFAULT_SIZE, payload.height)
        Assert.assertEquals(1, payload.batchCount)
        Assert.assertTrue(payload.nsfw)
        Assert.assertEquals(BonsaiBackend.VULKAN, payload.bonsaiBackend)

    }

    @Test
    fun `given text to image state, non Bonsai state with Vulkan Bonsai backend, expected payload uses Auto backend`() {
        // Given
        // The constructor arguments below specify the provider and generation options being mapped;
        // production mapToPayload() is the action.

        // When
        val payload = TextToImageState(
            mode = ServerSource.AUTOMATIC1111,
            bonsaiBackend = BonsaiBackend.VULKAN,
        ).mapToPayload()

        // Then
        Assert.assertEquals(BonsaiBackend.AUTO, payload.bonsaiBackend)

    }

    @Test
    fun `given text to image state, state switched to Bonsai, expected safe default size`() {
        // Given
        // The AUTOMATIC1111 state constructor below supplies 512 by 512 dimensions before the chained
        // withSource(BONSAI) action.

        // When
        val state = TextToImageState(
            mode = ServerSource.AUTOMATIC1111,
            width = "512",
            height = "512",
        ).withSource(
            source = ServerSource.LOCAL_APPLE_BONSAI,
            stableDiffusionSamplers = null,
            forgeModules = null,
            aDetailerAvailable = null,
            arliAiModels = null,
        )

        // Then
        Assert.assertEquals(BONSAI_DEFAULT_SIZE.toString(), state.width)
        Assert.assertEquals(BONSAI_DEFAULT_SIZE.toString(), state.height)

    }

    @Test
    fun `given text to image state, Bonsai state with non multiple width, expected validation error`() {
        // Given
        // The dimensionValidator fixture and explicit provider/dimension constructor inputs below define
        // this validation scenario.

        // When
        val state = TextToImageState(
            mode = ServerSource.LOCAL_APPLE_BONSAI,
            width = "100",
            height = "96",
        ).validated(dimensionValidator)

        // Then
        Assert.assertNotNull(state.widthValidationError)
        Assert.assertNull(state.heightValidationError)

    }

    @Test
    fun `given text to image state, Bonsai state with large size, expected no Bonsai specific max validation error`() {
        // Given
        // The dimensionValidator fixture and explicit provider/dimension constructor inputs below define
        // this validation scenario.

        // When
        val state = TextToImageState(
            mode = ServerSource.LOCAL_APPLE_BONSAI,
            width = "512",
            height = "256",
        ).validated(dimensionValidator)

        // Then
        Assert.assertNull(state.widthValidationError)
        Assert.assertNull(state.heightValidationError)

    }

    @Test
    fun `given text to image state, non Bonsai state with non multiple width, expected no multiple validation error`() {
        // Given
        // The dimensionValidator fixture and explicit provider/dimension constructor inputs below define
        // this validation scenario.

        // When
        val state = TextToImageState(
            mode = ServerSource.AUTOMATIC1111,
            width = "100",
            height = "96",
        ).validated(dimensionValidator)

        // Then
        Assert.assertNull(state.widthValidationError)
        Assert.assertNull(state.heightValidationError)

    }

    private companion object {
        const val MIN_SIZE = 64
        const val MAX_SIZE = 2048
    }
}
