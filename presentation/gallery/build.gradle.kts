plugins {
    alias(libs.plugins.generic.kmp.compose)
}

android {
    namespace = "com.shifthackz.aisdv1.presentation.gallery"
    testOptions.unitTests {
        isReturnDefaultValues = true
        isIncludeAndroidResources = true
    }
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

compose.resources {
    packageOfResClass = "com.shifthackz.aisdv1.presentation.gallery.generated.resources"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:ui"))
            implementation(project(":domain"))
            implementation(project(":presentation:components"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.animation)
            implementation(libs.compose.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.compose.activity)
            implementation(libs.koin.android)
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.compose.ui.tooling)
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

dependencies {
    debugImplementation(libs.test.compose.manifest)
}

// Reports describe compiler stability/skipping decisions; they do not measure frame performance.
composeCompiler {
    if (providers.gradleProperty("sdaiComposeReports").orNull == "true") {
        metricsDestination.set(layout.buildDirectory.dir("compose-metrics"))
        reportsDestination.set(layout.buildDirectory.dir("compose-reports"))
    }
}
