package com.shifthackz.aisdv1.presentation.logger.di

import com.shifthackz.aisdv1.presentation.logger.LogReader
import com.shifthackz.aisdv1.presentation.logger.LoggerPlatformActions
import com.shifthackz.aisdv1.presentation.logger.NoOpLogReader
import com.shifthackz.aisdv1.presentation.logger.NoOpLoggerPlatformActions
import org.koin.dsl.module


/**
 * Supplies singleton empty-log and inert native-action adapters for hosts without a log integration.
 *
 * @author Dmitriy Moroz
 */
val loggerCoreModule = module {
    single<LogReader> { NoOpLogReader }
    single<LoggerPlatformActions> { NoOpLoggerPlatformActions }
}
