plugins {
    alias(libs.plugins.generic.kmp.library)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android { namespace = "com.shifthackz.aisdv1.feature.openaioauth" }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":network"))
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.logging)
        }
        androidUnitTest.dependencies {
            implementation(libs.test.junit)
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.security.crypto)
        }
    }
}
