@file:Suppress("KotlinConstantConditions")

package com.shifthackz.aisdv1.feature.onnx.ai.tokenizer

import ai.onnxruntime.OnnxTensor
import android.text.TextUtils
import android.util.JsonReader
import android.util.Pair
import com.shifthackz.aisdv1.core.common.log.debugLog
import com.shifthackz.aisdv1.feature.onnx.LocalDiffusionContract
import com.shifthackz.aisdv1.feature.onnx.LocalDiffusionContract.KEY_INPUT_IDS
import com.shifthackz.aisdv1.feature.onnx.LocalDiffusionContract.TAG
import com.shifthackz.aisdv1.feature.onnx.ai.extensions.halfCorner
import com.shifthackz.aisdv1.feature.onnx.ai.extensions.toArrays
import com.shifthackz.aisdv1.feature.onnx.environment.OrtEnvironmentProvider
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxRuntimeConfiguration
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxRunController
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxSession
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxSessionFactory
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.IntBuffer
import java.util.Arrays
import java.util.Locale
import java.util.regex.Pattern

/**
 * Text encoder with job-scoped model configuration and explicitly owned session/input tensors.
 *
 * @param ortEnvironmentProvider Child native environment.
 * @param configuration Immutable model location.
 * @param controller Cooperative cancellation without concurrent session close.
 * @param sessionFactory Owns text-encoder sessions and their native options.
 *
 * @author Dmitriy Moroz
 */
internal class EnglishTextTokenizer(
    private val ortEnvironmentProvider: OrtEnvironmentProvider,
    private val configuration: OnnxRuntimeConfiguration,
    private val controller: OnnxRunController,
    private val sessionFactory: OnnxSessionFactory,
) : LocalDiffusionTextTokenizer {

    /**
     * Splits normalized prompt text into CLIP byte-pair token candidates.
     *
     * @author Dmitriy Moroz
     */
    private val pattern = Pattern.compile(TOKENIZER_REGEX)

    /**
     * Maps byte-pair tokens from this model's vocabulary to text-encoder token IDs.
     *
     * @author Dmitriy Moroz
     */
    private val encoder: MutableMap<String, Int> = HashMap()

    /**
     * Gives earlier model-defined byte-pair merges priority during prompt tokenization.
     *
     * @author Dmitriy Moroz
     */
    private val bpeRanks: MutableMap<Pair<String, String>, Int?> = HashMap()

    /**
     * Prevents reloading the immutable vocabulary when this job reopens its text session.
     *
     * @author Dmitriy Moroz
     */
    private var isInitMap = false

    /**
     * Owns the CPU text encoder and its options until positive and negative embeddings are copied.
     *
     * @author Dmitriy Moroz
     */
    private var session: OnnxSession? = null

    /**
     * Number of CLIP tokens, including beginning/end tokens and padding, in each encoder input.
     *
     * @author Dmitriy Moroz
     */
    override val maxLength = 77

    /**
     * Opens the CPU text encoder and loads this model's vocabulary and merge ranks.
     * File parsing and native model failures propagate; partially initialized jobs are closed by the owner.
     *
     * @author Dmitriy Moroz
     */
    override fun initialize() {
        if (session != null) {
            debugLog("{$TAG} {TOKENIZER} {initialize} Session already initialized, skipping...")
            return
        }
        controller.checkActive()
        session = sessionFactory.create(
            LocalDiffusionContract.TOKENIZER_MODEL,
            isAccelerationAllowed = false,
        )
        debugLog("{$TAG} {TOKENIZER} {initialize} Session created successfully!")
        if (!isInitMap) {
            encoder.putAll(loadEncoder())
            bpeRanks.putAll(loadBpeRanks())
        }
        isInitMap = true
        debugLog("{$TAG} {TOKENIZER} {initialize} Tokenizer map initialized successfully!")
    }

    /**
     * Normalizes prompt text, applies the model's byte-pair merges, then truncates/pads to 77 IDs.
     *
     * @param text Prompt text; null follows the existing tokenizer normalization behavior.
     * @return Fixed-length token IDs ending in the CLIP end-of-text token.
     * @author Dmitriy Moroz
     */
    override fun encode(text: String?): IntArray {
        var input = text
        input = input.toString().lowercase(Locale.getDefault()).halfCorner()
        val stringList: MutableList<String> = ArrayList()
        val matcher = pattern.matcher(input)
        while (matcher.find()) {
            val result = matcher.toMatchResult()
            val value = result.group().trim { it <= ' ' }
            val sb = StringBuilder()
            val bytes = value.toByteArray()
            val array = IntArray(bytes.size)
            for (i in array.indices) array[i] = bytes[i].toInt() and 0xff
            for (o in array) {
                if (TokenizerByteSet.byteEncoder.containsKey(o)) {
                    sb.append(TokenizerByteSet.byteEncoder[o])
                }
            }
            stringList.add(sb.toString())
        }
        val strings: MutableList<String> = ArrayList()
        for (string in stringList) {
            strings.addAll(bpe(string))
        }
        val result: MutableList<Int> = ArrayList()
        result.add(49406)
        for (word in strings) {
            if (encoder.containsKey(word)) {
                result.add(encoder[word]!!)
            }
        }
        val ids = IntArray(result.size)
        for (i in ids.indices) ids[i] = result[i]
        val copy = IntArray(maxLength)
        Arrays.fill(copy, 49407)
        System.arraycopy(ids, 0, copy, 0, if (ids.size < copy.size) ids.size else copy.size)
        copy[copy.size - 1] = 49407
        debugLog("{$TAG} {TOKENIZER} {encode} Encode was successful!")
        return copy
    }

    /**
     * Runs the CPU text encoder and copies its output into a caller-owned embedding tensor.
     *
     * @param ids Token IDs kept alive until the native encoder run completes, or null to skip encoding.
     * @return Independent embedding tensor that must be closed by the caller, or null for null IDs.
     * @author Dmitriy Moroz
     */
    override fun tensor(ids: IntArray?): OnnxTensor? {
        debugLog("{$TAG} {TOKENIZER} {tensor} Trying to tensor ${ids?.size ?: "null"} int array...")
        if (ids == null) {
            debugLog("{$TAG} {TOKENIZER} {tensor} Input ids array is null, skipping.")
            return null
        }
        return OnnxTensor.createTensor(
            ortEnvironmentProvider.get(), IntBuffer.wrap(ids), longArrayOf(1, ids.size.toLong()),
        ).use { inputIds ->
            controller.run(checkNotNull(session).value, mapOf(KEY_INPUT_IDS to inputIds))
                .use { result ->
                    OnnxTensor.createTensor(ortEnvironmentProvider.get(), result[0].value)
                }
        }
    }

    /**
     * Encodes the negative prompt using the same vocabulary and padding as the positive prompt.
     *
     * @param text Negative prompt submitted for classifier-free guidance.
     * @return Fixed-length token IDs for the negative conditioning branch.
     * @author Dmitriy Moroz
     */
    override fun createUnconditionalInput(text: String?): IntArray = encode(text)

    /**
     * Releases the text session and then its options after all borrowed encoder results are copied.
     *
     * @author Dmitriy Moroz
     */
    override fun close() {
        debugLog("{$TAG} {TOKENIZER} {close} Closing session...")
        val ownedSession = session
        session = null
        ownedSession?.close()
        debugLog("{$TAG} {TOKENIZER} {close} Session closed successfully!")
    }

    /**
     * Repeatedly merges the best-ranked adjacent byte pair until the model vocabulary has no merge.
     *
     * @param token Single normalized token from the CLIP tokenization pattern.
     * @return Byte-pair vocabulary entries, with the final entry carrying the end-of-word marker.
     * @author Dmitriy Moroz
     */
    private fun bpe(token: String): List<String> {
        if (TextUtils.isEmpty(token)) {
            return listOf(token)
        }
        var word: MutableList<String?> = token.toArrays().toMutableList()
        val lastItem = word.removeAt(word.size - 1)
        word.add("$lastItem</w>")
        var pairs = getPairs(word.toList())
        while (true) {
            var min: Pair<String, String>? = null
            var minValue = 0
            for (pair in pairs) {
                if (!bpeRanks.containsKey(pair)) {
                    continue
                }
                val value = bpeRanks[pair]!!
                if (min == null || value < minValue) {
                    min = pair
                    minValue = value
                }
            }
            if (min == null) break
            var i = 0
            val newWord: MutableList<String?> = ArrayList()
            while (i < word.size) {
                var j = -1
                for (x in word.indices) {
                    if (x >= i && word[x] == min.first) {
                        j = x
                        break
                    }
                }
                i = if (j != -1) {
                    newWord.addAll(word.subList(i, j).filterNotNull())
                    j
                } else {
                    newWord.addAll(word.subList(i, word.size).filterNotNull())
                    break
                }
                i += if (word[i] == min.first && i < word.size - 1 && word[i + 1] == min.second) {
                    newWord.add(min.first + min.second)
                    2
                } else {
                    word[i]?.let { newWord.add(it) }
                    1
                }
            }
            word = newWord
            pairs = if (word.size == 1) {
                break
            } else {
                getPairs(word)
            }
        }
        return word.filterNotNull()
    }

    /**
     * Collects distinct adjacent symbols for byte-pair merge ranking.
     *
     * @param word Current symbol sequence after any earlier merges.
     * @return Adjacent symbol pairs in their first-occurrence order.
     * @author Dmitriy Moroz
     */
    private fun getPairs(word: List<String?>): Set<Pair<String, String>> {
        val result: MutableSet<Pair<String, String>> = LinkedHashSet()
        for (i in 0 until word.size - 1) {
            result.add(Pair(word[i], word[i + 1]))
        }
        return result
    }

    /**
     * Reads the model's JSON token-to-integer vocabulary; malformed or missing files fail the job.
     *
     * @return Vocabulary keyed by byte-pair token spelling.
     * @author Dmitriy Moroz
     */
    private fun loadEncoder(): Map<String, Int> {
        val map: MutableMap<String, Int> = HashMap()
        val path = "${configuration.modelDirectory}/${LocalDiffusionContract.TOKENIZER_VOCABULARY}"
        JsonReader(InputStreamReader(FileInputStream(path))).use { jsonReader ->
            jsonReader.beginObject()
            while (jsonReader.hasNext()) {
                val key = jsonReader.nextName()
                val value = jsonReader.nextInt()
                map[key] = value
            }
        }
        return map
    }

    /**
     * Reads merge-pair precedence from the model's line-oriented CLIP merges file.
     *
     * @return Pair ranks in file order, excluding the version header.
     * @author Dmitriy Moroz
     */
    private fun loadBpeRanks(): Map<Pair<String, String>, Int?> {
        val result: MutableMap<Pair<String, String>, Int?> = HashMap()
        val path = "${configuration.modelDirectory}/${LocalDiffusionContract.TOKENIZER_MERGES}"
        BufferedReader(InputStreamReader(FileInputStream(path))).use { reader ->
            var line: String
            var startLine = 1
            var count = 0
            while (reader.readLine().also { ln -> line = ln ?: "" } != null) {
                if (startLine != 0 && startLine-- > 0) continue
                val array = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                if (array.size >= 2) {
                    result[Pair(array[0], array[1])] =
                        count++
                }
            }
        }
        return result
    }

    /**
     * CLIP-compatible token boundary pattern shared by every prompt in this job.
     *
     * @author Dmitriy Moroz
     */
    private companion object {
        /**
         * Recognizes contractions, Unicode letters/numbers, punctuation and whitespace runs.
         *
         * @author Dmitriy Moroz
         */
        private const val TOKENIZER_REGEX =
            "'s|'t|'re|'ve|'m|'ll|'d| ?\\p{L}+| ?\\p{N}+| ?[^\\s\\p{L}\\p{N}]+|\\s+(?!\\S)|\\s+"
    }
}
