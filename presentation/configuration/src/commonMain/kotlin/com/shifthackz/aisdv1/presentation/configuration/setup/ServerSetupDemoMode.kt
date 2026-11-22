package com.shifthackz.aisdv1.presentation.configuration.setup

import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Detects demo setup independently of the switch so entering the demo endpoint cannot skip its explanation.
 * Only AUTOMATIC1111 supports this sample-image mode; other providers retain their normal submission flow.
 *
 * @receiver Current candidate provider and connection fields.
 * @author Dmitriy Moroz
 */
internal val ServerSetupState.requiresDemoExplanation: Boolean
    get() = mode == ServerSource.AUTOMATIC1111 &&
            (demoMode || serverUrl.demoEndpointKey()
                ?.let { it == demoModeUrl.demoEndpointKey() } == true)

/**
 * Compares HTTP endpoint identities without changing a user-entered URL or exposing credentials.
 * Host/scheme case, default ports, surrounding whitespace and trailing path slashes are insignificant.
 * Query and fragment suffixes do not turn the demo endpoint into a different generation server.
 *
 * @receiver Candidate HTTP(S) URL.
 * @return Normalized endpoint identity, or null for missing/unsupported/malformed addresses.
 * @author Dmitriy Moroz
 */
private fun String.demoEndpointKey(): String? {
    val match = Regex("^(https?)://([^/?#\\s]+)([^?#]*)(?:[?#].*)?$", RegexOption.IGNORE_CASE)
        .matchEntire(trim()) ?: return null
    val scheme = match.groupValues[1].lowercase()
    val authority = match.groupValues[2].substringAfterLast('@').lowercase()
        .removeSuffix(if (scheme == "https") ":443" else ":80")
    if (authority.isBlank()) return null
    val path = match.groupValues[3].trimEnd('/')
    return "$scheme://$authority$path"
}
