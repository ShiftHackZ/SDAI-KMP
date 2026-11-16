package com.shifthackz.aisdv1.presentation.widget.engine

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Selects a checkpoint or explicitly refreshes the Easy Diffusion catalog.
 *
 * @param value Exact selected checkpoint ID; ignored for an explicit refresh.
 * @param shouldRefresh Whether to refresh the Easy Diffusion catalog instead of changing selection.
 * @author Dmitriy Moroz
 */
data class EngineSelectionIntent(val value: String = "", val shouldRefresh: Boolean = false) :
    MviIntent
