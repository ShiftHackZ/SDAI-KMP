plugins {
    alias(libs.plugins.generic.kmp.library)
}

android {
    namespace = "com.shifthackz.aisdv1.data"
    testOptions.unitTests.all { test ->
        test.jvmArgs(
            "--add-opens", "java.base/java.lang=ALL-UNNAMED",
            "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
        )
    }
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.test.coroutines)
        }
        commonMain.dependencies {
            implementation(project(":core:imageprocessing"))
            implementation(project(":core:common"))
            implementation(project(":domain"))
            implementation(project(":feature:openai-oauth"))
            implementation(project(":network"))
            implementation(project(":storage"))
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":domain"))
            implementation(project(":network"))
            implementation(project(":storage"))
            implementation(libs.koin.core)
            implementation(libs.koin.android)
            implementation(libs.androidx.security.crypto)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.google.gson)
            implementation(libs.okhttp.core)
        }
        iosMain.dependencies {
            implementation(project(":feature:mnn"))
        }
        androidUnitTest.dependencies {
            implementation(libs.test.junit)
            implementation(libs.test.mockito)
            implementation(libs.test.mockk)
            implementation(libs.test.roboelectric)
            implementation(libs.test.coroutines)
        }
    }
}
