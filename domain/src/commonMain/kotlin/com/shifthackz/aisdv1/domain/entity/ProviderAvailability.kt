package com.shifthackz.aisdv1.domain.entity

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider

/**
 * Providers supported by both the current build flavor and platform runtime prerequisites.
 *
 * @author Dmitriy Moroz
 */
fun BuildInfoProvider.setupAllowedModes(): List<ServerSource> = ServerSource.entries.filter {
    type in it.allowedInBuilds && platform in it.allowedPlatforms && isServerSourceAvailableOnPlatform(
        it
    )
}

/**
 * Checks runtime prerequisites without initializing inference or reading model files.
 *
 * @param source Provider considered for configuration or storage accounting.
 * @author Dmitriy Moroz
 */
expect fun isServerSourceAvailableOnPlatform(source: ServerSource): Boolean
