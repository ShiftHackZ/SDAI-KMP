package com.shifthackz.aisdv1.app.inference

import com.shifthackz.aisdv1.domain.entity.ProcessPerformanceReading

/**
 * Reads native-worker counters from inside that worker's sandbox.
 *
 * @author Dmitriy Moroz
 */
internal interface CurrentProcessPerformanceReader {
    /**
     * Reads CPU and resident memory without retaining a sampler; invoke off the control thread.
     * File and platform failures propagate to the requesting host as an unavailable reading.
     *
     * @author Dmitriy Moroz
     */
    fun read(): ProcessPerformanceReading
}
