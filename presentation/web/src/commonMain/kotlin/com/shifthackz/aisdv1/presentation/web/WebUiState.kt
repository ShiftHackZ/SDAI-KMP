package com.shifthackz.aisdv1.presentation.web

import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Selected provider identity and configured URL used by the embedded WebUI route.
 *
 * @param loading Whether provider configuration is still being prepared.
 * @param source Selected provider controlling title and special back-navigation policy.
 * @param url Configured WebUI base URL, empty when the provider has no embedded page.
 *
 * @author Dmitriy Moroz
 */
data class WebUiState(
    val loading: Boolean = true,
    val source: ServerSource = ServerSource.AUTOMATIC1111,
    val url: String = "",
) : MviState
