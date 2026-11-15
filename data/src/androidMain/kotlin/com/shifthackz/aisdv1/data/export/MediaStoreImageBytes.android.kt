package com.shifthackz.aisdv1.data.export

import kotlin.io.encoding.Base64
import kotlin.io.encoding.Base64.PaddingOption
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Decodes the original encoded image without changing its format or embedded metadata.
 * Accepts raw Base64 or a data URI containing `base64,`, with optional whitespace and trailing padding.
 *
 * @receiver Encoded image supplied by the provider or retained in the gallery.
 * @return The original image file bytes, including any metadata supplied upstream.
 * @throws IllegalArgumentException When the payload is not valid Base64.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalEncodingApi::class)
internal actual fun String.toMediaStoreImageBytes(): ByteArray =
    substringAfter("base64,", this)
        .filterNot(Char::isWhitespace)
        .let(Base64.Default.withPadding(PaddingOption.PRESENT_OPTIONAL)::decode)
