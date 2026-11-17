import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.android.tools.build.gradle)
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("Library") {
            id = "generic.library"
            implementationClass = "LibraryConventionPlugin"
        }
        register("Jacoco") {
            id = "generic.jacoco"
            implementationClass = "JacocoConventionPlugin"
        }
        register("Compose") {
            id = "generic.compose"
            implementationClass = "ComposeConventionPlugin"
        }
        register("KmpLibrary") {
            id = "generic.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("KmpCompose") {
            id = "generic.kmp.compose"
            implementationClass = "KmpComposeConventionPlugin"
        }
        register("Application") {
            id = "generic.application"
            implementationClass = "ApplicationConventionPlugin"
        }
        register("Flavors") {
            id = "generic.flavors"
            implementationClass = "FlavorsConventionPlugin"
        }
        register("BaselineProFm") {
            id = "generic.baseline.profm"
            implementationClass = "BaselineProFmConventionPlugin"
        }
    }
}
