plugins {
    alias(libs.plugins.generic.kmp.compose)
}

android {
    namespace = "com.shifthackz.aisdv1.presentation.components"
    testOptions.unitTests {
        all { test ->
            test.jvmArgs(
                "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
            )
        }
        isReturnDefaultValues = true
        isIncludeAndroidResources = true
    }
}

compose.resources {
    packageOfResClass = "com.shifthackz.aisdv1.presentation.components.generated.resources"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:ui"))
            implementation(project(":domain"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.animation)
            implementation(libs.compose.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.androidx.lifecycle.compose)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.compose.ui.tooling)
            implementation(libs.androidx.compose.activity)
            implementation(project(":core:notification"))
            implementation(libs.androidx.core)
            implementation(libs.koin.android)
        }

        iosMain.dependencies {
            implementation(libs.navigation.event.compose)
        }

        androidUnitTest.dependencies {
            implementation(libs.test.junit)
            implementation(libs.test.mockk)
            implementation(libs.test.coroutines)
            implementation(libs.test.roboelectric)
            implementation(libs.test.compose.junit)
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

dependencies {
    debugImplementation(libs.test.compose.manifest)
}
