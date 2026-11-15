package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * Fixed aggregate subtree labels. Gallery cells share a label and never allocate diagnostic owners.
 *
 * @author Dmitriy Moroz
 */
enum class UiDiagnosticSubtree {
    SCREEN,
    SAMPLER_FIELD,
    GENERATION_PROGRESS,
    GALLERY_GRID_IMAGE,
    GALLERY_DETAIL_IMAGE,
    HISTORY_IMAGE,
}
