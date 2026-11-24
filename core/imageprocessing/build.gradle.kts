plugins {
    alias(libs.plugins.generic.kmp.library)
}

android {
    namespace = "com.shifthackz.aisdv1.core.imageprocessing"
    testOptions.unitTests.isIncludeAndroidResources = true
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(project(":core:common"))
            implementation(libs.koin.core)
            implementation(libs.koin.android)
        }
        androidUnitTest.dependencies {
            implementation(libs.test.junit)
            implementation(libs.test.roboelectric)
        }
    }
}
