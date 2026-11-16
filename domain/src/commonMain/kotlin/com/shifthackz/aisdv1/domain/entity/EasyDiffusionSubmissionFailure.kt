package com.shifthackz.aisdv1.domain.entity

/**
 * The render response could not be recovered after submission. No second POST is sent and no global
 * stop is allowed because the owning positive task ID is unknown.
 * @param cause Original transport or decoding failure retained for diagnostics.
 * @author Dmitriy Moroz
 */
class EasyDiffusionSubmissionFailure(cause: Throwable) :
    Exception("Easy Diffusion submission outcome is unknown", cause)
