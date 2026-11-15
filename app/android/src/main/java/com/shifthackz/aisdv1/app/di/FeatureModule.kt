package com.shifthackz.aisdv1.app.di

import com.shifthackz.aisdv1.feature.auth.di.authModule
import com.shifthackz.aisdv1.feature.openaioauth.di.openAiOAuthModule
import com.shifthackz.aisdv1.feature.benchmark.di.benchmarkModule
import com.shifthackz.aisdv1.feature.bonsai.di.bonsaiModule
import com.shifthackz.aisdv1.feature.coreml.di.coreMlModule

val featureModule = arrayOf(
    authModule,
    openAiOAuthModule,
    benchmarkModule,
    bonsaiModule,
    coreMlModule,
)
