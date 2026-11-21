import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.generic.kmp.library)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

val providerContractOutput = layout.buildDirectory.file("provider-contracts/models.json")

tasks.register<JavaExec>("exportProviderContracts") {
    group = "verification"
    description =
        "Exports the compiled cloud-provider serialization contracts for upstream schema checks."
    val unitTests = tasks.named<Test>("testDebugUnitTest")
    dependsOn(
        "compileDebugUnitTestKotlinAndroid",
        "compileDebugUnitTestJavaWithJavac",
        "processDebugUnitTestJavaRes"
    )
    classpath =
        files(provider { unitTests.get().classpath }, provider { unitTests.get().testClassesDirs })
    mainClass.set("com.shifthackz.aisdv1.network.contract.ProviderContractExporter")
    args(providerContractOutput.get().asFile.absolutePath)
    outputs.file(providerContractOutput)
}

android {
    namespace = "com.shifthackz.aisdv1.network"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.core)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        androidUnitTest.dependencies {
            implementation(libs.test.junit)
            implementation(libs.test.mockk)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.server.test.host)
            implementation(libs.ktor.server.websockets)
        }
    }
}
