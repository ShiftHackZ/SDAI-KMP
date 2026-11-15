package com.shifthackz.aisdv1.domain.entity

/**
 * Controlled host-side failure when a native attempt cannot complete.
 * The category is safe to record; native exception messages are intentionally excluded.
 *
 * @param category Stable failure classification used by recovery and diagnostics.
 * @param engine Runtime actually attempted, independent of current provider selection.
 * @param backend Captured SDCPP backend, absent for other runtimes.
 * @author Dmitriy Moroz
 */
class NativeGenerationFailure(
    val category: Category,
    val engine: NativeGenerationEngine? = null,
    val backend: SdxlBackend? = null,
) : IllegalStateException(
    "Local generation failed (${category.name.lowercase()}). Please retry or select another backend.",
) {
    /**
     * Terminal reasons remain distinct from user cancellation and successful output.
     *
     * @author Dmitriy Moroz
     */
    enum class Category { BUSY, CONNECTION, PROCESS_DIED, UNRESPONSIVE, ENGINE, INVALID_RESULT, INTERRUPTED }
}
