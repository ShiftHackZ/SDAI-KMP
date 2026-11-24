import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

plugins {
    alias(libs.plugins.generic.kmp.compose)
}

compose.resources {
    packageOfResClass = "com.shifthackz.aisdv1.presentation.blog.generated.resources"
}

// The production Markdown renderer is compiled for Java 21.
val blogUnitTestLauncher = extensions.getByType<JavaToolchainService>().launcherFor {
    languageVersion.set(JavaLanguageVersion.of(21))
}

android {
    namespace = "com.shifthackz.aisdv1.presentation.blog"
    testOptions.unitTests {
        all { test ->
            test.javaLauncher.set(blogUnitTestLauncher)
            test.jvmArgs(
                "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",
            )
        }
        isReturnDefaultValues = true
        isIncludeAndroidResources = true
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
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.material3)
            implementation(libs.compose.resources)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.markdown.material3)
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.compose.ui.tooling)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
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
