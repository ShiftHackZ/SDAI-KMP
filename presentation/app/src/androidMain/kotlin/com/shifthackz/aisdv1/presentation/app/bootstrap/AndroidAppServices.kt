package com.shifthackz.aisdv1.presentation.app.bootstrap

import androidx.work.WorkerFactory
import com.shifthackz.aisdv1.domain.preference.PreferenceManager

/**
 * Typed access to Android startup collaborators without exposing the DI container.
 *
 * @author Dmitriy Moroz
 */
interface AndroidAppServices {
    /**
     * Lazily resolves the registered native worker factory after logging initialization.
     *
     * @author Dmitriy Moroz
     */
    val workerFactory: WorkerFactory

    /**
     * Lazily resolves persisted settings when native permission callbacks need them.
     *
     * @author Dmitriy Moroz
     */
    val preferenceManager: PreferenceManager
}
