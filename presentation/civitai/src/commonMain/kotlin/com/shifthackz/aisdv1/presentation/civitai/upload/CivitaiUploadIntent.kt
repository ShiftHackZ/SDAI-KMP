package com.shifthackz.aisdv1.presentation.civitai.upload

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Explicit gallery publication and login-prompt decisions.
 *
 * @author Dmitriy Moroz
 */
enum class CivitaiUploadIntent : MviIntent { UPLOAD, SIGN_IN, DISMISS }
