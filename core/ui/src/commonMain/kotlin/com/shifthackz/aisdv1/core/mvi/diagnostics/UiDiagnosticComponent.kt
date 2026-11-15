package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * Non-route owners such as modal dialogs, theme and background-work observers. These labels do not represent navigation destinations.
 *
 * @author Dmitriy Moroz
 */
enum class UiDiagnosticComponent : UiDiagnosticScreen {
    ROOT,
    EMBEDDING,
    DOWNLOAD_DIALOG,
    INPUT_HISTORY,
    EDIT_TAG,
    EXTRAS,
    AI_SD_APP_THEME,
    CONNECTIVITY,
    BACKGROUND_WORK,
    ENGINE_SELECTION,
}
