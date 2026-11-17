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
 * Supplies Android execution or the unsupported-platform implementation without native iOS linkage.
 *
 * @author Dmitriy Moroz
 */
internal expect fun platformMnnModule(): Module
