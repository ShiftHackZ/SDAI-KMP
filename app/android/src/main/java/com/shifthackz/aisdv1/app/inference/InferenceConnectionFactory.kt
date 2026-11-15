package com.shifthackz.aisdv1.app.inference

/**
 * Allocates independent host-side Binder/lifecycle state for each local attempt.
 *
 * @author Dmitriy Moroz
 */
internal interface InferenceConnectionFactory {
    /**
     * Creates a cheap unbound connection; no process or IO is started synchronously.
     *
     * @author Dmitriy Moroz
     */
    fun create(): InferenceConnection
}
