package com.shifthackz.aisdv1.feature.appupdate

import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.shifthackz.aisdv1.feature.appupdate.domain.AppUpdateGateway
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope

/**
 * Creates the gateway owned by this distribution; FOSS never contacts update services.
 *
 * @author Dmitriy Moroz
 */
internal fun Scope.createAndroidAppUpdateGateway(): AppUpdateGateway =
    PlayAppUpdateGateway(AppUpdateManagerFactory.create(androidContext()))
