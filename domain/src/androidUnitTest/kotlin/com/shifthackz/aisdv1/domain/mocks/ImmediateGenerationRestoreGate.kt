package com.shifthackz.aisdv1.domain.mocks

import com.shifthackz.aisdv1.domain.gateway.GenerationRestoreGate

/** Runs isolated generation component cases without the feature-owned process admission state. @author Dmitriy Moroz */
class ImmediateGenerationRestoreGate : GenerationRestoreGate {
    override suspend fun <T> generating(block: suspend () -> T): T = block()
    override suspend fun <T> restoring(block: suspend () -> T): T = block()
}
