package com.shifthackz.aisdv1.presentation.generation.img2img

import com.shifthackz.aisdv1.presentation.components.widget.image.OriginalImageBudget

/**
 * Center-crops an admitted original and normalizes it to at most 1536px JPEG at quality 95.
 * An already square source within that edge keeps its original bytes. The caller dispatches
 * this explicit transform off main; no sampled display bitmap participates in the operation.
 *
 * @param base64 Original encoded image, optionally prefixed by a data URI.
 * @param budget Encoded/pixel admission applied before full bitmap allocation.
 * @return Normalized original image bytes encoded as Base64.
 * @throws IllegalArgumentException when the encoded source or original dimensions exceed admission.
 * @author Dmitriy Moroz
 */
internal expect suspend fun cropBase64ImageToSquare(
    base64: String,
    budget: OriginalImageBudget
): String
