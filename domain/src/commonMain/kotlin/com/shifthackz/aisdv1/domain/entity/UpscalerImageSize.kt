package com.shifthackz.aisdv1.domain.entity

/**
 * Expected standalone output dimensions after proportional input preparation and the selected scale.
 *
 * @param width Output pixel width.
 * @param height Output pixel height.
 * @author Dmitriy Moroz
 */
data class UpscalerImageSize(val width: Int, val height: Int)
