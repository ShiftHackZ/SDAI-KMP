package com.shifthackz.aisdv1.presentation.logger.di

import com.shifthackz.aisdv1.presentation.logger.AndroidLogReader
import com.shifthackz.aisdv1.presentation.logger.AndroidLoggerPlatformActions
import com.shifthackz.aisdv1.presentation.logger.LogReader
import com.shifthackz.aisdv1.presentation.logger.LoggerPlatformActions
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module


/**
 * Supplies Android factories for reading the app log file and native clipboard/text-sharing actions.
 *
 * @author Dmitriy Moroz
 */
val loggerUiUtilsModule = module {
    factoryOf(::AndroidLogReader) bind LogReader::class
    factory { AndroidLoggerPlatformActions(androidContext()) } bind LoggerPlatformActions::class
}
