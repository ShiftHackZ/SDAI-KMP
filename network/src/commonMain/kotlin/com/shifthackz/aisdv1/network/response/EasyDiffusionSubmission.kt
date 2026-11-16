package com.shifthackz.aisdv1.network.response

import kotlinx.serialization.Serializable

/** Accepted render task identity; Python identity integers require 64-bit storage.
 * @param task Positive Python task identity returned by the accepted render POST.
 * @param stream Relative task stream route, checked against the accepted task identity.
 * @author Dmitriy Moroz
 */
@Serializable
data class EasyDiffusionSubmission(val task: Long, val stream: String)
