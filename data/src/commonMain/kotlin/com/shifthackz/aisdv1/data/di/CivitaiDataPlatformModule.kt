package com.shifthackz.aisdv1.data.di

import org.koin.core.module.Module

/**
 * Registers native protected storage without leaking native types into common contracts.
 *
 * @author Dmitriy Moroz
 */
internal expect val civitaiDataPlatformModule: Module
