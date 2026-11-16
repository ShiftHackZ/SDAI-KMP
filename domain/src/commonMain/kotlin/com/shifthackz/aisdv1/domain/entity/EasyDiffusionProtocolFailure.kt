package com.shifthackz.aisdv1.domain.entity

/**
 * Invalid provider response whose safe message can be displayed without JSON or image excerpts.
 * @param cause Original protocol failure retained for diagnostics; no fallback result is supplied.
 * @author Dmitriy Moroz
 */
class EasyDiffusionProtocolFailure(cause: Throwable) :
    Exception("Easy Diffusion returned an invalid response", cause)
