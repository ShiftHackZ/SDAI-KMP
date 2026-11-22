plugins {
    alias(libs.plugins.generic.kmp.library)
    alias(libs.plugins.generic.flavors)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.shifthackz.aisdv1.feature.appupdate"
    testOptions.unitTests.isReturnDefaultValues = true
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.koin.core)
        }
        androidUnitTest.dependencies {
            implementation(libs.test.junit)
            implementation(libs.test.coroutines)
            implementation(libs.test.mockk)
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
        }
        iosMain.dependencies { implementation(libs.ktor.client.darwin) }
    }
}

dependencies { "playstoreImplementation"(libs.google.play.app.update) }
