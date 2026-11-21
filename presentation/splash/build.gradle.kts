plugins {
    alias(libs.plugins.generic.kmp.compose)
}

android {
    namespace = "com.shifthackz.aisdv1.presentation.splash"
    testOptions.unitTests {
        all { test ->
            test.jvmArgs(
                "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
            )
        }
        isReturnDefaultValues = true
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:ui"))
            implementation(project(":domain"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }

        androidUnitTest.dependencies {
            implementation(libs.test.junit)
            implementation(libs.test.mockk)
            implementation(libs.test.coroutines)
        }
    }
}

// Reports describe compiler stability/skipping decisions; they do not measure frame performance.
composeCompiler {
    if (providers.gradleProperty("sdaiComposeReports").orNull == "true") {
        metricsDestination.set(layout.buildDirectory.dir("compose-metrics"))
        reportsDestination.set(layout.buildDirectory.dir("compose-reports"))
    }
}
