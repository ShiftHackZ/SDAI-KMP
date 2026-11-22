plugins {
    alias(libs.plugins.generic.kmp.compose)
    alias(libs.plugins.generic.flavors)
}

android {
    namespace = "com.shifthackz.aisdv1.presentation.appupdate"
    testOptions.unitTests {
        isReturnDefaultValues = true
        isIncludeAndroidResources = true
        all { test ->
            test.jvmArgs(
                "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",
            )
        }
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:appupdate"))
            implementation(project(":core:common"))
            implementation(project(":core:ui"))
            implementation(project(":core:localization"))
            implementation(project(":presentation:components"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
        androidUnitTest.dependencies {
            implementation(libs.test.junit)
            implementation(libs.test.coroutines)
            implementation(libs.test.mockk)
            implementation(libs.test.roboelectric)
            implementation(libs.test.compose.junit)
        }
        androidMain.dependencies {
            implementation(project(":domain"))
            implementation(libs.compose.material3)
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.compose.ui.tooling)
            implementation(libs.androidx.compose.activity)
            implementation(libs.androidx.lifecycle.compose)
        }
    }
}

dependencies { "playstoreImplementation"(libs.google.play.app.update) }

dependencies {
    "debugImplementation"(libs.test.compose.manifest)
}
