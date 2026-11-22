import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.generic.kmp.compose)
    alias(libs.plugins.generic.flavors)
}

android {
    namespace = "com.shifthackz.aisdv1.presentation.app"
}

kotlin {
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "AiSdPresentation"
            isStatic = true
            export(project(":feature:bonsai"))
            export(project(":feature:mnn"))
            export(project(":feature:coreml"))
            export(project(":feature:openai-oauth"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":presentation:benchmark"))
            implementation(project(":presentation:backup"))
            implementation(project(":presentation:components"))
            implementation(project(":presentation:appupdate"))
            implementation(project(":feature:appupdate"))
            implementation(project(":presentation:configuration"))
            implementation(project(":presentation:debug"))
            implementation(project(":presentation:donate"))
            implementation(project(":presentation:gallery"))
            implementation(project(":presentation:generation"))
            implementation(project(":presentation:history"))
            implementation(project(":presentation:home"))
            implementation(project(":presentation:logger"))
            implementation(project(":presentation:onboarding"))
            implementation(project(":presentation:report"))
            implementation(project(":presentation:settings"))
            implementation(project(":presentation:splash"))
            implementation(project(":presentation:statistics"))
            implementation(project(":presentation:web"))
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:ui"))
            implementation(project(":core:validation"))
            implementation(project(":domain"))
            implementation(project(":data"))
            implementation(project(":demo"))
            implementation(project(":network"))
            implementation(project(":feature:auth"))
            implementation(project(":feature:backup"))
            implementation(project(":feature:benchmark"))
            implementation(project(":feature:work"))
            api(project(":feature:bonsai"))
            api(project(":feature:mnn"))
            api(project(":feature:coreml"))
            api(project(":feature:openai-oauth"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.androidx.lifecycle.compose)
        }

        androidMain.dependencies {
            implementation(project(":core:imageprocessing"))
            implementation(project(":core:notification"))
            implementation(project(":feature:onnx"))
            implementation(project(":feature:mediapipe"))
            implementation(project(":feature:sdxl"))
            implementation(libs.androidx.compose.activity)
            implementation(libs.androidx.work.runtime)
            implementation(libs.koin.android)
        }

        androidUnitTest.dependencies {
            implementation(libs.test.junit)
            implementation(libs.test.mockk)
            implementation(libs.test.coroutines)
        }

        iosMain.dependencies {
            implementation(project(":feature:sdxl"))
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
