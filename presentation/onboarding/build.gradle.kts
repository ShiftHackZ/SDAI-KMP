plugins {
    alias(libs.plugins.generic.kmp.compose)
}

android {
    namespace = "com.shifthackz.aisdv1.presentation.onboarding"
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
            implementation(project(":core:localization"))
            implementation(project(":core:ui"))
            implementation(project(":domain"))
            implementation(project(":presentation:components"))
            implementation(project(":presentation:configuration"))
            implementation(project(":presentation:generation"))
            implementation(project(":presentation:settings"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.animation)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.compose.activity)
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.compose.ui.tooling)
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
