package com.shifthackz.aisdv1.domain.usecase.gallery

import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway

/**
 * Reads storage information for the gallery export destination.
 *
 * @param mediaStoreGateway Provides platform-specific media storage information.
 *
 * @author Dmitriy Moroz
 */
internal class GetMediaStoreInfoUseCaseImpl(
    private val mediaStoreGateway: MediaStoreGateway,
) : GetMediaStoreInfoUseCase {

    /**
     * Returns the platform media storage information.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke() = mediaStoreGateway.getInfo()
}
