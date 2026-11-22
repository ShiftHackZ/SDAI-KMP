package com.shifthackz.aisdv1.work.fixture

import com.shifthackz.aisdv1.work.IosWorkExecution

/**
 * Retains native callbacks so ownership and admission races can be exercised deterministically.
 *
 * @author Dmitriy Moroz
 */
internal class RecordingIosWorkExecution : IosWorkExecution {
    val readiness = linkedMapOf<String, () -> Unit>()
    val expirations = mutableMapOf<String, () -> Unit>()
    val rejections = mutableMapOf<String, () -> Unit>()
    val gpuRequirements = mutableMapOf<String, Boolean>()
    val progress = mutableMapOf<String, Int?>()
    val outcomes = mutableMapOf<String, Boolean>()

    /**
     * Captures callbacks without admitting work until the scenario releases its owner.
     *
     * @param ownerId Unique operation whose callbacks must remain independent.
     * @param title Initial system heading, unused by these lifecycle checks.
     * @param subtitle Initial system detail, unused by these lifecycle checks.
     * @param requiresGpu Resource requirement inspected before admitting downloads.
     * @param onExpiration Callback used to expire exactly one captured operation.
     * @param onReady Callback released manually after admission assertions.
     * @param onUnavailable Callback released when native admission is denied.
     * @author Dmitriy Moroz
     */
    override fun begin(
        ownerId: String,
        title: String,
        subtitle: String,
        requiresGpu: Boolean,
        onExpiration: () -> Unit,
        onReady: () -> Unit,
        onUnavailable: () -> Unit
    ) {
        readiness[ownerId] = onReady
        expirations[ownerId] = onExpiration
        rejections[ownerId] = onUnavailable
        gpuRequirements[ownerId] = requiresGpu
    }

    /**
     * Records each owner's latest aggregate percentage independently.
     *
     * @param ownerId Operation receiving the update.
     * @param title Localized heading unused by percentage assertions.
     * @param subtitle Current model name unused by percentage assertions.
     * @param percentage Aggregate measured progress, including unknown measurements.
     * @author Dmitriy Moroz
     */
    override fun update(ownerId: String, title: String, subtitle: String, percentage: Int?) {
        progress[ownerId] = percentage
    }

    /**
     * Records the final outcome after download cleanup has returned.
     *
     * @param ownerId Operation releasing its native session.
     * @param isSuccess Whether the complete operation installed its selected models.
     * @param title Localized outcome heading unused by lifecycle assertions.
     * @param subtitle Final model detail unused by lifecycle assertions.
     * @author Dmitriy Moroz
     */
    override fun end(ownerId: String, isSuccess: Boolean, title: String, subtitle: String) {
        outcomes[ownerId] = isSuccess
    }
}
