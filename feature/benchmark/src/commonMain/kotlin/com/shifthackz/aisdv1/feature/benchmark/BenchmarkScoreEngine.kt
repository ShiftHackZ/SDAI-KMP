package com.shifthackz.aisdv1.feature.benchmark

import com.shifthackz.aisdv1.domain.policy.benchmark.BenchmarkRecommendationPolicy
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkPlatform
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkAccelerator
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkAccelerationStatus
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkDeviceInfo
import com.shifthackz.aisdv1.domain.entity.benchmark.accelerationCapabilities
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkResult
import com.shifthackz.aisdv1.core.common.time.TimeProvider
import com.shifthackz.aisdv1.domain.entity.ServerSource
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Runs a bounded synthetic benchmark and derives conservative recommendations.
 *
 * The benchmark intentionally avoids model inference and GPU execution, so it
 * cannot crash due to missing model files or unstable AI runtimes.
 *
 * @param timeProvider Measures elapsed workload duration for throughput scoring.
 *
 * @author Dmitriy Moroz
 */
internal class BenchmarkScoreEngine(
    private val timeProvider: TimeProvider,
) {

    fun score(deviceInfo: BenchmarkDeviceInfo): BenchmarkResult {
        val workload = inferenceLikeScore(deviceInfo)
        val cpu = workload.score
        val memory = memoryScore()
        val accelerator = acceleratorScore(deviceInfo)
        val total = (
                cpu * CPU_WEIGHT +
                        memory * MEMORY_WEIGHT +
                        accelerator * ACCELERATOR_WEIGHT +
                        ramScore(deviceInfo) * RAM_WEIGHT
                )
            .let { weighted -> weighted * deviceClassFactor(deviceInfo) }
            .roundToInt()
            .coerceIn(MIN_TOTAL_SCORE, MAX_TOTAL_SCORE)
        val providerRecommendations = BenchmarkRecommendationPolicy.providerRecommendations(
            deviceInfo = deviceInfo,
            totalScore = total,
            cpuScore = cpu,
            memoryScore = memory,
            acceleratorScore = accelerator,
        )
        val recommendation =
            BenchmarkRecommendationPolicy.fallbackRecommendation(providerRecommendations)
        return BenchmarkResult(
            createdAt = timeProvider.currentTimeMillis(),
            deviceInfo = deviceInfo,
            cpuScore = cpu,
            memoryScore = memory,
            acceleratorScore = accelerator,
            totalScore = total,
            estimatedTimeSeconds = providerRecommendations
                .filter { it.recommended && it.estimatedTimeSeconds > 0 }
                .minOfOrNull { it.estimatedTimeSeconds }
                ?: BenchmarkRecommendationPolicy.estimateGenerationSeconds(
                    provider = recommendation.providers.firstOrNull()
                        ?: ServerSource.LOCAL_MICROSOFT_ONNX,
                    width = recommendation.width,
                    height = recommendation.height,
                    samplingSteps = recommendation.samplingSteps,
                    totalScore = total,
                    deviceInfo = deviceInfo,
                ),
            recommendation = recommendation,
            providerRecommendations = providerRecommendations,
            notes = notes(deviceInfo, workload),
        )
    }

    private fun inferenceLikeScore(deviceInfo: BenchmarkDeviceInfo): WorkloadScore {
        val workload = InferenceLikeWorkload()
        var checksum = workload.runPass(0)
        var passes = 0
        val targetMs = when {
            BenchmarkRecommendationPolicy.isPixel3aClass(deviceInfo) -> PIXEL_3A_TARGET_MS
            BenchmarkRecommendationPolicy.effectiveRamMb(deviceInfo) < 4_000L -> LOW_MEMORY_TARGET_MS
            else -> DEFAULT_TARGET_MS
        }
        val startedAt = timeProvider.nanoTime()
        var elapsedMs: Double
        do {
            checksum += workload.runPass(passes + 1)
            passes++
            elapsedMs = (timeProvider.nanoTime() - startedAt) / 1_000_000.0
        } while (elapsedMs < targetMs && passes < MAX_WORKLOAD_PASSES)
        if (checksum == Float.NEGATIVE_INFINITY) error("Unreachable")
        val throughput = passes * WORKLOAD_UNITS_PER_PASS / max(elapsedMs, 1.0)
        val coreFactor = sqrt(deviceInfo.cpuCores.coerceAtLeast(1).toDouble()).coerceIn(1.0, 3.2)
        val score = (throughput * INFERENCE_SCORE_SCALE * coreFactor)
            .roundToInt()
            .coerceIn(MIN_COMPONENT_SCORE, MAX_COMPONENT_SCORE)
        return WorkloadScore(
            score = score,
            elapsedMs = elapsedMs.roundToInt(),
            passes = passes,
        )
    }

    private fun memoryScore(): Int {
        val size = MEMORY_BLOCK_MB * 1024 * 1024
        val source = ByteArray(size) { index -> (index % 127).toByte() }
        val target = ByteArray(size)
        var checksum = 0
        val elapsedMs = measureMs {
            repeat(MEMORY_COPY_PASSES) { iteration ->
                source.copyInto(target)
                var index = iteration * MEMORY_STRIDE % MEMORY_STRIDE_WINDOW
                while (index < size) {
                    checksum = checksum xor target[index].toInt()
                    index += MEMORY_STRIDE
                }
            }
        }
        if (checksum == Int.MIN_VALUE) error("Unreachable")
        val mbCopied = size * MEMORY_COPY_PASSES / (1024.0 * 1024.0)
        val mbPerSecond = mbCopied / (max(elapsedMs, 1.0) / 1_000.0)
        return (mbPerSecond * MEMORY_SCORE_SCALE)
            .roundToInt()
            .coerceIn(MIN_COMPONENT_SCORE, MAX_COMPONENT_SCORE)
    }

    private fun acceleratorScore(deviceInfo: BenchmarkDeviceInfo): Int {
        val base = when (deviceInfo.platform) {
            BenchmarkPlatform.ANDROID -> 260
            BenchmarkPlatform.IOS -> 720
            BenchmarkPlatform.UNKNOWN -> 250
        }
        return deviceInfo.accelerationCapabilities()
            .filter { it.status == BenchmarkAccelerationStatus.SUPPORTED }
            .fold(base) { score, capability ->
                score + when (capability.accelerator) {
                    BenchmarkAccelerator.VULKAN -> 180
                    BenchmarkAccelerator.BONSAI_VULKAN -> 220
                    BenchmarkAccelerator.OPEN_CL -> 320
                    BenchmarkAccelerator.NNAPI -> 240
                    BenchmarkAccelerator.METAL -> 760
                    BenchmarkAccelerator.CORE_ML -> 900
                    BenchmarkAccelerator.NEURAL_ENGINE -> 1_200
                }
            }.coerceIn(MIN_COMPONENT_SCORE, MAX_COMPONENT_SCORE)
    }

    private fun ramScore(deviceInfo: BenchmarkDeviceInfo): Int =
        (BenchmarkRecommendationPolicy.effectiveRamMb(deviceInfo) * RAM_SCORE_SCALE)
            .roundToInt()
            .coerceIn(MIN_COMPONENT_SCORE, MAX_COMPONENT_SCORE)

    private fun deviceClassFactor(deviceInfo: BenchmarkDeviceInfo): Float = when {
        BenchmarkRecommendationPolicy.isPixel3aClass(deviceInfo) -> 0.56f
        deviceInfo.platform == BenchmarkPlatform.ANDROID &&
                BenchmarkRecommendationPolicy.effectiveRamMb(deviceInfo) < 4_000L -> 0.72f

        deviceInfo.platform == BenchmarkPlatform.ANDROID -> 0.92f
        deviceInfo.platform == BenchmarkPlatform.IOS -> 1.38f
        else -> 0.75f
    }

    private fun notes(
        deviceInfo: BenchmarkDeviceInfo,
        workload: WorkloadScore,
    ): List<String> = buildList {
        deviceInfo.acceleratorDiagnostics.forEach(::add)
        if (deviceInfo.totalVramMb == null) add("VRAM is not directly exposed by this platform.")
        if (deviceInfo.accelerationCapabilities()
                .none { it.status == BenchmarkAccelerationStatus.SUPPORTED }
        ) {
            add("No supported local hardware acceleration backend was detected.")
        }
        add(
            "CPU score uses an inference-like workload with convolution, normalization, " +
                    "attention-style matrix math, and decoder mixing.",
        )
        add("Workload completed ${workload.passes} passes in ${workload.elapsedMs} ms.")
        add("Memory score uses block copy plus strided access.")
        add("No AI runtime, model file, or GPU inference pipeline is initialized during benchmark.")
        if (deviceInfo.accelerationCapabilities()
                .any { it.status == BenchmarkAccelerationStatus.NOT_VALIDATED }
        ) {
            add("Some acceleration APIs are present, but the local runtime backend is not validated for them.")
        }
    }

    private fun measureMs(block: () -> Unit): Double {
        val startedAt = timeProvider.nanoTime()
        block()
        return (timeProvider.nanoTime() - startedAt) / 1_000_000.0
    }

    private companion object {
        private const val CPU_WEIGHT = 0.62f
        private const val MEMORY_WEIGHT = 0.23f
        private const val ACCELERATOR_WEIGHT = 0.05f
        private const val RAM_WEIGHT = 0.10f
        private const val MIN_TOTAL_SCORE = 150
        private const val MAX_TOTAL_SCORE = 12_000
        private const val MIN_COMPONENT_SCORE = 120
        private const val MAX_COMPONENT_SCORE = 8_000
        private const val WORKLOAD_UNITS_PER_PASS = 10_500
        private const val INFERENCE_SCORE_SCALE = 0.095
        private const val DEFAULT_TARGET_MS = 6_000.0
        private const val LOW_MEMORY_TARGET_MS = 5_000.0
        private const val PIXEL_3A_TARGET_MS = 6_500.0
        private const val MAX_WORKLOAD_PASSES = 96
        private const val MEMORY_BLOCK_MB = 8
        private const val MEMORY_COPY_PASSES = 48
        private const val MEMORY_STRIDE = 4096
        private const val MEMORY_STRIDE_WINDOW = MEMORY_STRIDE * 16
        private const val MEMORY_SCORE_SCALE = 1.15
        private const val RAM_SCORE_SCALE = 0.55f
    }
}

private data class WorkloadScore(
    val score: Int,
    val elapsedMs: Int,
    val passes: Int,
)


/**
 * Small deterministic workload shaped like diffusion inference stages.
 *
 * It intentionally uses plain Kotlin arrays, bounded memory, and no model
 * files, GPU contexts, delegates, or native runtime initialization.
 *
 * @author Dmitriy Moroz
 */
private class InferenceLikeWorkload {

    private var input = FloatArray(TENSOR_SIZE) { index ->
        ((index * 31 % 257) - 128) / 128f
    }
    private var output = FloatArray(TENSOR_SIZE)
    private val weights = FloatArray(CHANNELS * CHANNELS * KERNEL_SIZE) { index ->
        ((index * 17 % 37) - 18) / 240f
    }
    private val bias = FloatArray(CHANNELS) { index ->
        ((index % 5) - 2) / 32f
    }
    private val tokens = FloatArray(TOKEN_COUNT * ATTENTION_DIM)
    private val query = FloatArray(TOKEN_COUNT * ATTENTION_DIM)
    private val key = FloatArray(TOKEN_COUNT * ATTENTION_DIM)
    private val value = FloatArray(TOKEN_COUNT * ATTENTION_DIM)
    private val attention = FloatArray(TOKEN_COUNT * TOKEN_COUNT)
    private val context = FloatArray(TOKEN_COUNT * ATTENTION_DIM)
    private val projection = FloatArray(ATTENTION_DIM * ATTENTION_DIM) { index ->
        ((index * 13 % 29) - 14) / 180f
    }

    fun runPass(seed: Int): Float {
        var checksum = 0f
        repeat(RESIDUAL_BLOCKS) { block ->
            convolution3x3()
            groupNormSiluResidual(block)
            swapBuffers()
            checksum += input[(seed * 977 + block * 131) % input.size]
        }
        checksum += attentionBlock(seed)
        checksum += decoderMix(seed)
        return checksum
    }

    private fun convolution3x3() {
        for (channel in 0 until CHANNELS) {
            val offset = channel * PLANE
            for (index in 0 until PLANE) output[offset + index] = 0f
        }
        for (outChannel in 0 until CHANNELS) {
            val outputOffset = outChannel * PLANE
            for (y in 1 until LATENT_SIZE - 1) {
                val row = y * LATENT_SIZE
                for (x in 1 until LATENT_SIZE - 1) {
                    var sum = bias[outChannel]
                    for (inChannel in 0 until CHANNELS) {
                        val inputOffset = inChannel * PLANE
                        val weightOffset = (outChannel * CHANNELS + inChannel) * KERNEL_SIZE
                        sum += input[inputOffset + row - LATENT_SIZE + x - 1] * weights[weightOffset]
                        sum += input[inputOffset + row - LATENT_SIZE + x] * weights[weightOffset + 1]
                        sum += input[inputOffset + row - LATENT_SIZE + x + 1] * weights[weightOffset + 2]
                        sum += input[inputOffset + row + x - 1] * weights[weightOffset + 3]
                        sum += input[inputOffset + row + x] * weights[weightOffset + 4]
                        sum += input[inputOffset + row + x + 1] * weights[weightOffset + 5]
                        sum += input[inputOffset + row + LATENT_SIZE + x - 1] * weights[weightOffset + 6]
                        sum += input[inputOffset + row + LATENT_SIZE + x] * weights[weightOffset + 7]
                        sum += input[inputOffset + row + LATENT_SIZE + x + 1] * weights[weightOffset + 8]
                    }
                    output[outputOffset + row + x] = sum
                }
            }
        }
    }

    private fun groupNormSiluResidual(block: Int) {
        val channelsPerGroup = CHANNELS / GROUPS
        for (group in 0 until GROUPS) {
            val startChannel = group * channelsPerGroup
            val endChannel = startChannel + channelsPerGroup
            var sum = 0f
            var count = 0
            for (channel in startChannel until endChannel) {
                val offset = channel * PLANE
                for (index in 0 until PLANE) {
                    sum += output[offset + index]
                    count++
                }
            }
            val mean = sum / count
            var variance = 0f
            for (channel in startChannel until endChannel) {
                val offset = channel * PLANE
                for (index in 0 until PLANE) {
                    val delta = output[offset + index] - mean
                    variance += delta * delta
                }
            }
            val inverseStd = (1.0 / sqrt((variance / count + EPSILON).toDouble())).toFloat()
            for (channel in startChannel until endChannel) {
                val offset = channel * PLANE
                for (index in 0 until PLANE) {
                    val position = offset + index
                    val normalized = (output[position] - mean) * inverseStd
                    val activated = normalized / (1f + exp((-normalized).toDouble()).toFloat())
                    output[position] =
                        input[position] * RESIDUAL_WEIGHT + activated * (1f - RESIDUAL_WEIGHT) +
                                block * 0.000_13f
                }
            }
        }
    }

    private fun attentionBlock(seed: Int): Float {
        sampleTokens(seed)
        project(tokens, query, 0)
        project(tokens, key, 7)
        project(tokens, value, 13)
        val scale = (1.0 / sqrt(ATTENTION_DIM.toDouble())).toFloat()
        for (token in 0 until TOKEN_COUNT) {
            var rowMax = -Float.MAX_VALUE
            for (other in 0 until TOKEN_COUNT) {
                var dot = 0f
                val queryOffset = token * ATTENTION_DIM
                val keyOffset = other * ATTENTION_DIM
                for (dim in 0 until ATTENTION_DIM) {
                    dot += query[queryOffset + dim] * key[keyOffset + dim]
                }
                val score = dot * scale
                attention[token * TOKEN_COUNT + other] = score
                if (score > rowMax) rowMax = score
            }
            var normalizer = 0f
            val attentionOffset = token * TOKEN_COUNT
            for (other in 0 until TOKEN_COUNT) {
                val probability =
                    exp((attention[attentionOffset + other] - rowMax).toDouble()).toFloat()
                attention[attentionOffset + other] = probability
                normalizer += probability
            }
            val inverseNormalizer = 1f / normalizer
            val contextOffset = token * ATTENTION_DIM
            for (dim in 0 until ATTENTION_DIM) context[contextOffset + dim] = 0f
            for (other in 0 until TOKEN_COUNT) {
                val probability = attention[attentionOffset + other] * inverseNormalizer
                val valueOffset = other * ATTENTION_DIM
                for (dim in 0 until ATTENTION_DIM) {
                    context[contextOffset + dim] += probability * value[valueOffset + dim]
                }
            }
        }
        var checksum = 0f
        for (token in 0 until TOKEN_COUNT) {
            for (dim in 0 until ATTENTION_DIM) {
                val channel = dim % CHANNELS
                val y = (token * 5 + dim * 3 + seed) % LATENT_SIZE
                val x = (token * 3 + dim * 7 + seed) % LATENT_SIZE
                val tensorIndex = channel * PLANE + y * LATENT_SIZE + x
                val value = context[token * ATTENTION_DIM + dim]
                input[tensorIndex] += value * ATTENTION_WRITE_WEIGHT
                checksum += value
            }
        }
        return checksum
    }

    private fun sampleTokens(seed: Int) {
        for (token in 0 until TOKEN_COUNT) {
            for (dim in 0 until ATTENTION_DIM) {
                val channel = dim % CHANNELS
                val y = (token * 7 + dim * 5 + seed) % LATENT_SIZE
                val x = (token * 11 + dim * 3 + seed) % LATENT_SIZE
                tokens[token * ATTENTION_DIM + dim] = input[channel * PLANE + y * LATENT_SIZE + x]
            }
        }
    }

    private fun project(
        source: FloatArray,
        target: FloatArray,
        shift: Int,
    ) {
        for (token in 0 until TOKEN_COUNT) {
            val tokenOffset = token * ATTENTION_DIM
            for (dim in 0 until ATTENTION_DIM) {
                var sum = 0f
                for (inner in 0 until ATTENTION_DIM) {
                    val projectionIndex = ((dim + shift) % ATTENTION_DIM) * ATTENTION_DIM + inner
                    sum += source[tokenOffset + inner] * projection[projectionIndex]
                }
                target[tokenOffset + dim] = sum
            }
        }
    }

    private fun decoderMix(seed: Int): Float {
        var checksum = 0f
        for (channel in 0 until CHANNELS) {
            val offset = channel * PLANE
            for (y in 1 until LATENT_SIZE - 1) {
                val row = y * LATENT_SIZE
                for (x in 1 until LATENT_SIZE - 1) {
                    val index = offset + row + x
                    val mixed = input[index] * 0.62f +
                            input[index - 1] * 0.11f +
                            input[index + 1] * 0.11f +
                            input[index - LATENT_SIZE] * 0.08f +
                            input[index + LATENT_SIZE] * 0.08f
                    output[index] = mixed
                    if ((index + seed) % CHECKSUM_STRIDE == 0) checksum += mixed
                }
            }
        }
        swapBuffers()
        return checksum
    }

    private fun swapBuffers() {
        val previous = input
        input = output
        output = previous
    }

    private companion object {
        private const val LATENT_SIZE = 32
        private const val CHANNELS = 16
        private const val PLANE = LATENT_SIZE * LATENT_SIZE
        private const val TENSOR_SIZE = CHANNELS * PLANE
        private const val KERNEL_SIZE = 9
        private const val GROUPS = 4
        private const val RESIDUAL_BLOCKS = 4
        private const val TOKEN_COUNT = 96
        private const val ATTENTION_DIM = 32
        private const val EPSILON = 1e-5f
        private const val RESIDUAL_WEIGHT = 0.22f
        private const val ATTENTION_WRITE_WEIGHT = 0.000_7f
        private const val CHECKSUM_STRIDE = 251
    }
}

/**
 * Derives provider-specific settings from benchmark measurements.
 *
 * @author Dmitriy Moroz
 */
