package com.shifthackz.aisdv1.presentation.configuration.setup.mappers

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.model.isAvailableOn

/**
 * Providers supported by both the current distribution flavor and native platform.
 *
 * @author Dmitriy Moroz
 */
val BuildInfoProvider.allowedModes: List<ServerSource>
    get() = ServerSource
        .entries
        .filter { source ->
            source.allowedInBuilds.contains(type) &&
                    source.isAvailableOn(platform)
        }
