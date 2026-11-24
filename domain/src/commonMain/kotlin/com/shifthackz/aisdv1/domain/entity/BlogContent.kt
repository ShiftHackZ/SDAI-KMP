package com.shifthackz.aisdv1.domain.entity

/**
 * Blog feed available to the reader during one screen visit.
 *
 * @param posts Articles in publication order.
 * @param isCached Whether articles came from the saved snapshot.
 * @author Dmitriy Moroz
 */
data class BlogContent(val posts: List<BlogPost>, val isCached: Boolean)
