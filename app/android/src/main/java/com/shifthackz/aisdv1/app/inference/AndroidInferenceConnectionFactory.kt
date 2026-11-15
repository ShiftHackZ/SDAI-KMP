package com.shifthackz.aisdv1.app.inference

import android.content.Context

/**
 * Host composition factory; connection collaborators are never directly built by UI code.
 *
 * @param context Host application context.
 * @author Dmitriy Moroz
 */
internal class AndroidInferenceConnectionFactory(private val context: Context) :
    InferenceConnectionFactory {
    /**
     * Creates state for exactly one child binding.
     *
     * @author Dmitriy Moroz
     */
    override fun create(): InferenceConnection = AndroidInferenceConnection(context)
}
