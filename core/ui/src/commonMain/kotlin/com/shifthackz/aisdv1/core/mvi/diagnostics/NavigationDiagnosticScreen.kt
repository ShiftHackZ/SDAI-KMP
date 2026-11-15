package com.shifthackz.aisdv1.core.mvi.diagnostics

import com.shifthackz.aisdv1.core.navigation.NavigationDestination

/**
 * Fixed destination wrapper cached once per enum value; route arguments never enter diagnostics.
 *
 * @param destination Code-owned navigation identity.
 * @author Dmitriy Moroz
 */
private data class NavigationDiagnosticScreen(val destination: NavigationDestination) :
    UiDiagnosticScreen {
    override val name: String get() = destination.name
}

private val diagnosticDestinations =
    NavigationDestination.entries.associateWith(::NavigationDiagnosticScreen)

/**
 * Returns the cached diagnostic owner for a shared destination without allocating per observation.
 *
 * @author Dmitriy Moroz
 */
fun NavigationDestination.asDiagnosticScreen(): UiDiagnosticScreen =
    diagnosticDestinations.getValue(this)
