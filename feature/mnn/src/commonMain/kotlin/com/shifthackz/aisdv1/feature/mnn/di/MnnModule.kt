package com.shifthackz.aisdv1.feature.mnn.di

import org.koin.core.module.Module

/**
 * MNN runtime bindings behind the same common API on every platform.
 * Android installs these only in its isolated child inference graph.
 *
 * @author Dmitriy Moroz
 */
val mnnModule: Module = platformMnnModule()

/**
 * Installs only platform host dependencies; Android native bindings remain child-only.
 *
 * @author Dmitriy Moroz
 */
val mnnHostRuntimeModule: Module = platformMnnHostModule()

/**
 * Supplies the in-process Apple binding or an empty Android host module.
 *
 * @author Dmitriy Moroz
 */
internal expect fun platformMnnHostModule(): Module

/**
 * Supplies isolated Android execution or injected in-process Apple execution.
 *
 * @author Dmitriy Moroz
 */
internal expect fun platformMnnModule(): Module
