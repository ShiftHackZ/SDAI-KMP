plugins {
    alias(libs.plugins.generic.kmp.compose)
}

android {
    namespace = "com.shifthackz.aisdv1.core.ui"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:navigation"))
            implementation(project(":core:common"))
            implementation(libs.koin.core)
            implementation(libs.androidx.lifecycle.compose)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.runtime)
            implementation(compose.ui)
            // BaseMviViewModel exposes ViewModel as its public superclass.
            api(libs.androidx.lifecycle.viewmodel.core)
            implementation(libs.kotlinx.coroutines.core)
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
