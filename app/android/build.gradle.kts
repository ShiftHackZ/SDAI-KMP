import java.io.FileInputStream
import java.time.Instant
import java.util.Properties

plugins {
    alias(libs.plugins.generic.application)
    alias(libs.plugins.generic.baseline.profm)
    alias(libs.plugins.compose.compiler)
}

// Seconds since 2020 keep nightly updates ordered independently of stable versions.
val devVersionCode = providers.gradleProperty("sdaiDevVersionCode")
    .map(String::toInt)
    .getOrElse((Instant.now().epochSecond - 1_577_836_800L).toInt())
val devVersionSuffix = providers.gradleProperty("sdaiDevVersionSuffix")
    .getOrElse("-dev.local")

android {
    namespace = "com.shifthackz.aisdv1.app"

    buildTypes {
        getByName("release") {
            ndk.debugSymbolLevel = "FULL"
            buildConfigField("boolean", "UI_DIAGNOSTICS_ENABLED", "false")
        }
        getByName("debug") {
            buildConfigField(
                "boolean",
                "UI_DIAGNOSTICS_ENABLED",
                "true"
            )
        }
        create("profile") {
            initWith(getByName("release"))
            matchingFallbacks += "release"
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".profile"
            buildConfigField(
                "boolean",
                "UI_DIAGNOSTICS_ENABLED",
                "true"
            )
        }
        create("dev") {
            initWith(getByName("release"))
            matchingFallbacks += "release"
            isDebuggable = false
            applicationIdSuffix = ".dev"
            versionNameSuffix = devVersionSuffix
            resValue("string", "app_name", "SDAI Dev")
        }
    }

    // Startup providers are disabled; select an explicit optional tracer at the composition root.
    sourceSets {
        listOf("release", "dev").forEach { variant ->
            getByName(variant).java.srcDir("src/tracingDisabled/java")
        }
        listOf("debug", "profile").forEach { variant ->
            getByName(variant).java.srcDir(
                if (providers.gradleProperty("sdaiComposeTracing").orNull == "true") {
                    "src/tracingEnabled/java"
                } else {
                    "src/tracingDisabled/java"
                },
            )
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    defaultConfig {
        applicationId = "com.shifthackz.aisdv1.app"
        versionName = libs.versions.versionName.get()
        versionCode = libs.versions.versionCode.get().toInt()

        buildConfigField("String", "IMAGE_CDN_URL", "\"https://random.imagecdn.app/\"")
        buildConfigField("String", "HUGGING_FACE_URL", "\"https://huggingface.co/\"")
        buildConfigField(
            "String",
            "HUGGING_FACE_INFERENCE_URL",
            "\"https://router.huggingface.co/hf-inference/\""
        )
        buildConfigField("String", "OPEN_AI_URL", "\"https://api.openai.com/\"")
        buildConfigField("String", "STABILITY_AI_URL", "\"https://api.stability.ai/\"")

        buildConfigField("String", "UPDATE_API_URL", "\"https://sdai.moroz.cc\"")
        buildConfigField("String", "REPORT_API_URL", "\"https://sdai-report.moroz.cc\"")

        resourceConfigurations += listOf("en", "ru", "uk", "tr", "zh", "de")
        manifestPlaceholders["excludePermissions"] = "true"
    }

    productFlavors {
        getByName("foss") {
            ndk {
                //noinspection ChromeOsAbiSupport
                abiFilters += "arm64-v8a"
            }
        }
    }

    val signingPropertiesFile = rootProject.file("app/keystore/signing.properties")
    if (signingPropertiesFile.exists()) {
        val props = Properties()
        props.load(FileInputStream(signingPropertiesFile))
        val alias = props["keystore.alias"] as String
        val keystorePath = props["keystore"] as String
        signingConfigs {
            create("release") {
                storeFile = rootProject.file("app/$keystorePath")
                storePassword = props["keystore.password"] as String
                keyAlias = alias
                keyPassword = props["keystore.password"] as String
            }
        }
        println("[Signature] -> Build will be signed with signature: $alias")
        buildTypes.getByName("release").signingConfig = signingConfigs.getByName("release")
        buildTypes.getByName("dev").signingConfig = signingConfigs.getByName("release")
    }
}

androidComponents {
    beforeVariants(selector().withBuildType("dev")) { variant ->
        variant.enable = variant.productFlavors.any { (_, flavor) -> flavor == "full" }
    }
    onVariants(selector().withBuildType("dev")) { variant ->
        require(devVersionCode in 1..2_100_000_000) {
            "sdaiDevVersionCode must be between 1 and 2100000000."
        }
        variant.outputs.forEach { output ->
            output.versionCode.set(devVersionCode)
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":presentation:app"))
    implementation(project(":domain"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.core.splashscreen)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.activity)
    implementation(project(":presentation:components"))
    implementation(libs.timber)
    implementation(libs.shifthackz.catppuccin.splash)
    implementation(libs.shifthackz.catppuccin.legacy)
    implementation(libs.androidx.work.runtime)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.coroutines)
    testImplementation(libs.test.roboelectric)
}

// Reports describe compiler stability/skipping decisions; they do not measure frame performance.
composeCompiler {
    if (providers.gradleProperty("sdaiComposeReports").orNull == "true") {
        metricsDestination.set(layout.buildDirectory.dir("compose-metrics"))
        reportsDestination.set(layout.buildDirectory.dir("compose-reports"))
    }
}

// Runtime tracing is absent from ordinary product dependencies.
if (providers.gradleProperty("sdaiComposeTracing").orNull == "true") {
    dependencies {
        add("debugImplementation", "androidx.compose.runtime:runtime-tracing")
        add("profileImplementation", "androidx.compose.runtime:runtime-tracing")
    }
}
