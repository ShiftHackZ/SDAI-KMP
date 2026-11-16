package com.shifthackz.aisdv1.network.api.easydiffusion

/**
 * Marks invalid response JSON without exposing decoder excerpts as the public exception message.
 * @param cause Original decoding failure retained for diagnostics.
 * @author Dmitriy Moroz
 */
class EasyDiffusionProtocolException(cause: Throwable) :
    Exception("Easy Diffusion returned an invalid response", cause)
