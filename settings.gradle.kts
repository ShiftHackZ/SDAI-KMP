pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "SDAI"

val modules = listOf(
    ":app",
    ":core:common",
    ":core:imageprocessing",
    ":core:localization",
    ":core:notification",
    ":core:navigation",
    ":core:ui",
    ":core:validation",
    ":data",
    ":demo",
    ":domain",
    ":feature:auth",
    ":feature:openai-oauth",
    ":feature:benchmark",
    ":feature:bonsai",
    ":feature:coreml",
    ":feature:onnx",
    ":feature:mediapipe",
    ":feature:mnn",
    ":feature:sdxl",
    ":feature:work",
    ":network",
    ":presentation:app",
    ":presentation:benchmark",
    ":presentation:components",
    ":presentation:configuration",
    ":presentation:debug",
    ":presentation:donate",
    ":presentation:gallery",
    ":presentation:generation",
    ":presentation:history",
    ":presentation:home",
    ":presentation:logger",
    ":presentation:onboarding",
    ":presentation:report",
    ":presentation:settings",
    ":presentation:splash",
    ":presentation:statistics",
    ":presentation:web",
    ":storage",
)

include(modules)

project(":app").projectDir = file("app/android")
