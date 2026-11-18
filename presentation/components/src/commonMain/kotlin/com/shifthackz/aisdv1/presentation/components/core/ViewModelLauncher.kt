package com.shifthackz.aisdv1.presentation.components.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job

/**
 * Aliases `ViewModelLauncher` for SDAI presentation code.
 *
 * @author Dmitriy Moroz
 */
typealias ViewModelLauncher = (
    CoroutineDispatcher,
    CoroutineStart,
    suspend CoroutineScope.() -> Unit,
) -> Job
