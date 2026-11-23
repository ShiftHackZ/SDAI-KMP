package com.shifthackz.aisdv1.presentation.donate.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.presentation.donate.DonateState

/**
 * Supplies loading, empty, varied supporter content and scrolled acknowledgement lists.
 *
 * @author Dmitriy Moroz
 */
internal class DonatePreviewParameterProvider : PreviewParameterProvider<DonatePreviewCase> {
    private val supporter = DonatePreviewFixtures.supporter
    private val populated = DonateState(loading = false, supporters = listOf(supporter))
    private val cases = listOf(
        DonatePreviewCase("Loading", DonateState()),
        DonatePreviewCase("Empty · Also used after load failure", DonateState(loading = false)),
        DonatePreviewCase("Single supporter", populated),
        DonatePreviewCase(
            "Without message",
            populated.copy(supporters = listOf(supporter.copy(message = "")))
        ),
        DonatePreviewCase(
            "Blank message",
            populated.copy(supporters = listOf(supporter.copy(message = "   ")))
        ),
        DonatePreviewCase(
            "Multiline message", populated.copy(
                supporters = listOf(
                    supporter.copy(message = "Thank you for the app.\nIt is great to create images on the go.\nKeep up the good work!"),
                )
            )
        ),
        DonatePreviewCase(
            "Long message", populated.copy(
                supporters = listOf(
                    supporter.copy(
                        message = "Thank you for continuing to improve this project and making creative tools available to more people. ".repeat(
                            4
                        )
                    ),
                )
            )
        ),
        DonatePreviewCase(
            "Long name", populated.copy(
                supporters = listOf(
                    supporter.copy(name = "A supporter with a long display name"),
                )
            )
        ),
        DonatePreviewCase(
            "Unicode name and message", populated.copy(
                supporters = listOf(
                    supporter.copy(name = "Олексій", message = "Дякую! 🎨 ありがとうございます！"),
                )
            )
        ),
        DonatePreviewCase(
            "Blank name",
            populated.copy(supporters = listOf(supporter.copy(name = "")))
        ),
        DonatePreviewCase(
            "Unformatted date", populated.copy(
                supporters = listOf(
                    supporter.copy(date = "Nov 2026"),
                )
            )
        ),
        DonatePreviewCase(
            "Mixed supporters",
            populated.copy(supporters = DonatePreviewFixtures.mixed)
        ),
        DonatePreviewCase(
            "Long list · Top",
            populated.copy(supporters = DonatePreviewFixtures.many)
        ),
        DonatePreviewCase(
            "Long list · Lower rows",
            populated.copy(supporters = DonatePreviewFixtures.many),
            firstVisibleItem = 12
        ),
    )

    /**
     * Fixed acknowledgement states displayed in both application palettes.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<DonatePreviewCase> = cases.asSequence()

    /**
     * Identifies the loading, supporter-text or list-position scenario.
     *
     * @param index Position in the ordered donation-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
