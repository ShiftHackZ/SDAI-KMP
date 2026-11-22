package com.shifthackz.aisdv1.presentation.app

import com.shifthackz.aisdv1.core.navigation.NavigationEntry
import com.shifthackz.aisdv1.core.navigation.isHomeRoute

/**
 * Describes a push or pop between existing entry identities; replacements have no inherited history.
 *
 * @param outgoing Entry whose foreground is being replaced.
 * @param incoming Entry becoming the foreground.
 * @param isPop Whether the outgoing screen moves away to reveal its predecessor.
 * @author Dmitriy Moroz
 */
internal data class NavigationMotion(
    val outgoing: NavigationEntry,
    val incoming: NavigationEntry,
    val isPop: Boolean,
)

/**
 * Determines animation direction from entry lifetimes without comparing route arguments or indices.
 *
 * @param previous Entries retained before the router update.
 * @param current Latest authoritative router history.
 * @return Push/pop motion, or null for initialization, unchanged selection and replacements.
 * @author Dmitriy Moroz
 */
internal fun navigationMotion(
    previous: List<NavigationEntry>,
    current: List<NavigationEntry>,
): NavigationMotion? {
    val outgoing = previous.lastOrNull() ?: return null
    val incoming = current.lastOrNull() ?: return null
    if (outgoing.id == incoming.id) return null
    if (outgoing.route.isHomeRoute() && incoming.route.isHomeRoute()) return null
    return when {
        current.any { it.id == outgoing.id } -> NavigationMotion(outgoing, incoming, false)
        previous.any { it.id == incoming.id } -> NavigationMotion(outgoing, incoming, true)
        else -> null
    }
}
