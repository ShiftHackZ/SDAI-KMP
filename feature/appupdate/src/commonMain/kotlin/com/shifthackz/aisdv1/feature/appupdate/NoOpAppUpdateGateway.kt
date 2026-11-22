package com.shifthackz.aisdv1.feature.appupdate

import com.shifthackz.aisdv1.feature.appupdate.domain.AppUpdateGateway

/**
 * Leaves version selection entirely to the distribution manager without making requests.
 *
 * @author Dmitriy Moroz
 */
internal object NoOpAppUpdateGateway : AppUpdateGateway {
    override suspend fun check() = null
}
