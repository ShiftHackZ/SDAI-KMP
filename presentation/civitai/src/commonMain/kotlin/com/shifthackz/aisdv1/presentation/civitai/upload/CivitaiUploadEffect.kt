package com.shifthackz.aisdv1.presentation.civitai.upload

import com.shifthackz.aisdv1.core.mvi.MviEffect

/**
 * Native navigation requested only after an explicit user action.
 *
 * @author Dmitriy Moroz
 */
sealed interface CivitaiUploadEffect : MviEffect {
    data object SignIn : CivitaiUploadEffect

    /**
     * Opens a successfully recorded public post.
     *
     * @param url Fixed-origin Civitai post URL.
     * @author Dmitriy Moroz
     */
    data class OpenPost(val url: String) : CivitaiUploadEffect
}
