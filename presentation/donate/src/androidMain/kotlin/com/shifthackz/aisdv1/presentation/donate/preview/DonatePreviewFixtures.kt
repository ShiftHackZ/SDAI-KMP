package com.shifthackz.aisdv1.presentation.donate.preview

import com.shifthackz.aisdv1.domain.entity.Supporter

/**
 * Supplies fictional supporter acknowledgements with fixed dates and no account or network access.
 *
 * @author Dmitriy Moroz
 */
internal object DonatePreviewFixtures {
    val supporter = Supporter(
        id = 1,
        name = "Sample Supporter",
        date = "2026-11-01",
        message = "Thank you for making image generation available on mobile!",
    )
    val mixed = listOf(
        supporter,
        supporter.copy(id = 2, name = "Alex", message = ""),
        supporter.copy(id = 3, name = "Олексій", message = "Дякую за застосунок! 💛"),
        supporter.copy(
            id = 4,
            name = "Riley",
            message = "Keep creating.\nLooking forward to the next update."
        ),
    )
    val many = List(24) { index ->
        supporter.copy(
            id = index + 1,
            name = "Sample Supporter ${index + 1}",
            message = if (index % 2 == 0) supporter.message else "",
        )
    }
}
