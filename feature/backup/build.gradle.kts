plugins {
    alias(libs.plugins.generic.kmp.library)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}
android { namespace = "com.shifthackz.aisdv1.feature.backup" }
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(project(":storage"))
            implementation(project(":core:common"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.okio)
        }
        androidMain.dependencies { implementation(libs.koin.android) }
        androidUnitTest.dependencies {
            implementation(libs.test.junit)
            implementation(libs.test.mockk)
            implementation(libs.test.coroutines)
        }
    }
}
