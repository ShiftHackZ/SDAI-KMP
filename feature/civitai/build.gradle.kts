plugins {
    alias(libs.plugins.generic.kmp.library)
}

android {
    namespace = "com.shifthackz.aisdv1.feature.civitai"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.appauth)
            implementation(libs.koin.android)
        }
        androidUnitTest.dependencies {
            implementation(libs.test.junit)
            implementation(libs.test.mockk)
            implementation(libs.test.coroutines)
        }
        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(libs.koin.core)
            implementation(libs.ktor.client.core)
            implementation(libs.okio)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

val civitaiClientId = providers.gradleProperty("civitaiClientId")
val generatedCivitaiBuildConfig =
    layout.buildDirectory.dir("generated/source/civitaiBuildConfig/commonMain/kotlin")
val generateCivitaiBuildConfig by tasks.registering {
    inputs.property("clientId", civitaiClientId)
    outputs.dir(generatedCivitaiBuildConfig)
    doLast {
        val clientId = civitaiClientId.get()
        require(clientId.matches(Regex("[a-fA-F0-9-]{36}"))) { "Invalid Civitai public client ID" }
        val output = generatedCivitaiBuildConfig.get()
            .file("com/shifthackz/aisdv1/feature/civitai/config/BuildConfig.kt").asFile
        output.parentFile.mkdirs()
        output.writeText(
            """
            package com.shifthackz.aisdv1.feature.civitai.config

            /** Public OAuth configuration generated for Android and iOS. */
            object BuildConfig {
                const val CIVITAI_CLIENT_ID: String = "$clientId"
                const val CIVITAI_REDIRECT_URI: String = "cc.moroz.sdai://oauth/civitai"
            }
            """.trimIndent() + "\n"
        )
    }
}
kotlin.sourceSets.commonMain { kotlin.srcDir(generateCivitaiBuildConfig) }
