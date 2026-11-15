package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * A bounded diagnostic owner identity. Route identities wrap the shared navigation destination; helper identities never accept arbitrary strings.
 *
 * @author Dmitriy Moroz
 */
sealed interface UiDiagnosticScreen {
    /**
     * Fixed export and trace label with no route arguments.
     *
     * @author Dmitriy Moroz
     */
    val name: String
}
