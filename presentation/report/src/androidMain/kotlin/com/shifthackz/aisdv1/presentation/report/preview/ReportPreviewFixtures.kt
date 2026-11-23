package com.shifthackz.aisdv1.presentation.report.preview

import com.shifthackz.aisdv1.presentation.report.ReportState

/**
 * Small local report samples shared by state and phone-size previews; no gallery records are read.
 *
 * @author Dmitriy Moroz
 */
internal object ReportPreviewFixtures {
    /**
     * A 32-pixel checkerboard PNG used to show the report thumbnail without the image cache service.
     *
     * @author Dmitriy Moroz
     */
    private const val ImageBase64 =
        "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAIAAAD8GO2jAAAAQklEQVR4nGO4c+IrVpQXugorIlU9w6gFoxYMAQuoZRAu9aMWjFowFCyglkG41I9aMGrBULCAWgbhUj9qwagFQ8ACAP32DHmKhrEjAAAAAElFTkSuQmCC"

    /**
     * Editable report with an image and the default reason, before any description is entered.
     *
     * @author Dmitriy Moroz
     */
    val ready = ReportState(loading = false, imageBase64 = ImageBase64)

    /**
     * Neutral example text for the filled draft and submission-error previews.
     *
     * @author Dmitriy Moroz
     */
    const val Description =
        "The generated image includes an unexpected watermark. Please review it."

    /**
     * Multiline draft used to inspect scrolling, wrapping and submit-button placement.
     *
     * @author Dmitriy Moroz
     */
    val longDescription = List(6) { Description }.joinToString("\n\n")
}
