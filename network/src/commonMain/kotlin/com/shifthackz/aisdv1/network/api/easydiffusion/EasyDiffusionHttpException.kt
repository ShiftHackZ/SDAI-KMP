package com.shifthackz.aisdv1.network.api.easydiffusion

/** HTTP failure that retains only a safe status for domain recovery policy.
 * @param statusCode HTTP status retained for domain recovery decisions, without response bodies or URLs.
 * @author Dmitriy Moroz */
class EasyDiffusionHttpException(val statusCode: Int) : Exception("Easy Diffusion HTTP $statusCode")
