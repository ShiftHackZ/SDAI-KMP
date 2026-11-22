package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.entity.MnnBackend

/**
 * Supported requested backends shared by setup, presentation and execution admission.
 *
 * @author Dmitriy Moroz
 */
interface MnnBackendPolicy {
    /**
     * Ordered platform-supported choices; device admission still checks actual runtime availability.
     *
     * @author Dmitriy Moroz
     */
    val supportedBackends: List<MnnBackend>
}
