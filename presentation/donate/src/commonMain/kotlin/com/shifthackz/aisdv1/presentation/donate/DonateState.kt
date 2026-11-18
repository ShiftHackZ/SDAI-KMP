package com.shifthackz.aisdv1.presentation.donate

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.Supporter

/**
 * Supporter acknowledgements and loading status for the donation page.
 *
 * @param loading Whether the supporter list is being fetched.
 * @param supporters Supporter records available for acknowledgement rendering.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class DonateState(
    val loading: Boolean = true,
    val supporters: List<Supporter> = emptyList(),
) : MviState
