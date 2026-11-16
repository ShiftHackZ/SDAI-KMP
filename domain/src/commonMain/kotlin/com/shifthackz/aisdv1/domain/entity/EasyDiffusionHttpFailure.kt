package com.shifthackz.aisdv1.domain.entity

/**
 * Safe HTTP status failure without response bodies, URLs or image data.
 * @param statusCode HTTP status retained for domain recovery decisions, without response bodies or URLs.
 * @author Dmitriy Moroz
 */
class EasyDiffusionHttpFailure(val statusCode: Int) : Exception("Easy Diffusion HTTP $statusCode")
