package com.shifthackz.aisdv1.presentation.civitai.upload

import com.shifthackz.aisdv1.core.mvi.MviState

/**
 * User-visible publication state without images or credentials.
 *
 * @param phase Current confirmation/progress/result phase.
 * @author Dmitriy Moroz
 */
data class CivitaiUploadState(val phase: CivitaiUploadPhase = CivitaiUploadPhase.IDLE) : MviState
