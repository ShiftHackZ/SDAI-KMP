package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.MnnBackend

/**
 * Selects backend vocabulary without probing GPU availability during UI construction.
 *
 * @param buildInfoProvider Application platform supplied by the composition root.
 *
 * @author Dmitriy Moroz
 */
internal class MnnBackendPolicyImpl(buildInfoProvider: BuildInfoProvider) : MnnBackendPolicy {
    override val supportedBackends = if (buildInfoProvider.platform == Platform.IOS)
        listOf(MnnBackend.CPU, MnnBackend.METAL) else listOf(MnnBackend.CPU, MnnBackend.OPENCL)
}
