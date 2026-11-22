package com.shifthackz.aisdv1.presentation.civitai.history

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shifthackz.aisdv1.domain.entity.CivitaiUpload
import com.shifthackz.aisdv1.domain.entity.CivitaiUploadStatus
import com.shifthackz.aisdv1.presentation.civitai.preview.CivitaiPreviewTheme

/**
 * Shows the full-screen first-upload artwork and message in both app palettes.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Empty history · Light", widthDp = 360, heightDp = 740, showBackground = true)
@Preview(
    name = "Empty history · Dark",
    widthDp = 360,
    heightDp = 740,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Empty history · Compact large text",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.3f
)
@Composable
private fun CivitaiEmptyHistoryPreview() {
    CivitaiHistoryPreview(CivitaiHistoryState(isLoading = false))
}

/**
 * Shows the initial eight shimmer rows before receipt identities are loaded.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Loading history · Light", widthDp = 360, heightDp = 740, showBackground = true)
@Preview(
    name = "Loading history · Dark",
    widthDp = 360,
    heightDp = 740,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CivitaiLoadingHistoryPreview() {
    CivitaiHistoryPreview(CivitaiHistoryState(isLoading = true))
}

/**
 * Shows a published link, unresolved upload outcomes and one pending viewport metadata row.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Upload history · Light", widthDp = 360, heightDp = 740, showBackground = true)
@Preview(
    name = "Upload history · Dark",
    widthDp = 360,
    heightDp = 740,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CivitaiPopulatedHistoryPreview() {
    val uploads = CivitaiUploadStatus.entries.mapIndexed { index, status ->
        CivitaiUpload(
            id = index + 1L,
            imageId = index + 100L,
            accountId = 1,
            url = if (status == CivitaiUploadStatus.PUBLISHED) "https://civitai.com/posts/1001" else null,
            createdAt = 1_700_000_000_000L + index,
            imageCreatedAt = 1_699_999_000_000L + index,
            imageRevision = "preview-$index",
            status = status,
        )
    }
    CivitaiHistoryPreview(
        CivitaiHistoryState(
            itemIds = uploads.map { it.id } + 5L,
            uploads = uploads,
            isLoading = false,
        )
    )
}

/**
 * Shows the retry row and storage-failure explanation when receipt loading fails.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "History error", widthDp = 360, heightDp = 740, showBackground = true)
@Composable
private fun CivitaiHistoryErrorPreview() {
    CivitaiHistoryPreview(CivitaiHistoryState(isLoading = false, hasFailed = true))
}

/**
 * Renders fixed history state without database observation, navigation or browser side effects.
 *
 * @param state Receipt, loading and error state illustrated by the preview.
 * @author Dmitriy Moroz
 */
@Composable
private fun CivitaiHistoryPreview(state: CivitaiHistoryState) {
    CivitaiPreviewTheme {
        CivitaiHistoryContent(state = state, processIntent = {}, openUrl = {})
    }
}
