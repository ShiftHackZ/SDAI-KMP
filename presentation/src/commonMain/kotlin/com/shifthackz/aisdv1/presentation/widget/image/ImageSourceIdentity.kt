package com.shifthackz.aisdv1.presentation.widget.image

/**
 * Referential key avoids comparing or hashing a full Base64 string during recomposition.
 *
 * @param source Original immutable encoded source retained by its screen owner.
 * @author Dmitriy Moroz
 */
internal class ImageSourceIdentity(private val source: String?) {
    override fun equals(other: Any?): Boolean =
        other is ImageSourceIdentity && source === other.source

    override fun hashCode(): Int = 0
}
