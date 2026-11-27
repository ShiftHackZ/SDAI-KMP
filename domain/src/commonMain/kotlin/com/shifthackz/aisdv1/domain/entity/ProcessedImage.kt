package com.shifthackz.aisdv1.domain.entity

/**
 * Encoded pixels and authoritative dimensions after image preparation.
 *
 * @param image Base64-encoded source or lossless crop.
 * @param width Prepared pixel width.
 * @param height Prepared pixel height.
 * @author Dmitriy Moroz
 */
data class ProcessedImage(val image: String, val width: Int, val height: Int)
