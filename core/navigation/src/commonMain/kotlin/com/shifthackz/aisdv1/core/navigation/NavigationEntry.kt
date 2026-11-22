package com.shifthackz.aisdv1.core.navigation

/**
 * Identifies one lifetime in the host's navigation history, including repeated visits to a route.
 *
 * @param id Host-local identity retained until this entry is removed or replaced.
 * @param route Destination and in-memory arguments owned by this entry.
 * @author Dmitriy Moroz
 */
data class NavigationEntry(val id: Long, val route: AppRoute)
