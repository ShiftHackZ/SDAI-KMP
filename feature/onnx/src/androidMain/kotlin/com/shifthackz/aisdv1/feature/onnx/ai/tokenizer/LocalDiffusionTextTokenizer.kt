package com.shifthackz.aisdv1.feature.onnx.ai.tokenizer

import ai.onnxruntime.OnnxTensor

/**
 * Tokenizes model-specific CLIP prompts and owns their CPU text-encoder session for one job.
 *
 * @author Dmitriy Moroz
 */
internal interface LocalDiffusionTextTokenizer {
    /**
     * Fixed token count expected by the text encoder, including special tokens and padding.
     *
     * @author Dmitriy Moroz
     */
    val maxLength: Int

    /**
     * Opens the captured model's text encoder and loads its vocabulary; failures propagate.
     *
     * @author Dmitriy Moroz
     */
    fun initialize()

    /**
     * Produces a padded/truncated token sequence for the model's CLIP text encoder.
     *
     * @param text Positive or negative prompt to normalize and tokenize.
     * @return Fixed-length token IDs including beginning/end tokens.
     * @author Dmitriy Moroz
     */
    fun encode(text: String?): IntArray?

    /**
     * Runs the text encoder and transfers an independent embedding tensor to the caller.
     *
     * @param ids Fixed-length prompt tokens; null skips the native run.
     * @return Caller-owned embedding tensor, or null when no IDs were supplied.
     * @author Dmitriy Moroz
     */
    fun tensor(ids: IntArray?): OnnxTensor?

    /**
     * Tokenizes the negative conditioning branch for classifier-free guidance.
     *
     * @param text Negative prompt in the same format as the positive prompt.
     * @return Fixed-length token IDs for the negative conditioning branch.
     * @author Dmitriy Moroz
     */
    fun createUnconditionalInput(text: String?): IntArray?

    /**
     * Releases the native session and options after all borrowed outputs have been copied.
     *
     * @author Dmitriy Moroz
     */
    fun close()
}
