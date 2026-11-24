package com.shifthackz.aisdv1.domain.entity

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Defers provider persistence until the optional image postprocessing stage has completed.
 * The request coroutine owns this marker, so concurrent unrelated operations remain unaffected.
 *
 * @author Dmitriy Moroz
 */
class GenerationFinalizationContext : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<GenerationFinalizationContext>
}
