package com.shifthackz.aisdv1.buildlogic

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.HasUnitTest
import com.android.build.api.variant.ScopedArtifacts
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Registers coverage for enabled debug variants using their production class artifacts.
 * CI selects debug for plain modules and fullDebug for modules with product flavors.
 * Shared Kotlin sources come from the Android compilation, including dependsOn source sets.
 *
 * @author Dmitriy Moroz
 */
internal fun Project.jacocoCodeCoverageReporting() {
    val androidComponents =
        extensions.getByName("androidComponents") as AndroidComponentsExtension<*, *, *>
    androidComponents.onVariants(androidComponents.selector().withBuildType("debug")) { variant ->
        if ((variant as? HasUnitTest)?.unitTest == null) return@onVariants

        val capitalizedVariant = variant.name.replaceFirstChar(Char::titlecase)
        val testTaskName = "test${capitalizedVariant}UnitTest"
        val report = tasks.register<VariantCoverageReport>("jacoco${capitalizedVariant}Report") {
            group = "verification"
            description = "Runs $testTaskName and reports Android/JVM coverage for ${variant.name}."
            dependsOn(testTaskName)

            reports {
                xml.required.set(true)
                html.required.set(true)
                csv.required.set(false)
            }

            sourceDirectories.from(variant.sources.java?.all, variant.sources.kotlin?.all)
            sourceDirectories.from(provider {
                this@jacocoCodeCoverageReporting.extensions
                    .findByType(KotlinMultiplatformExtension::class.java)
                    ?.targets?.getByName("android")?.compilations?.getByName(variant.name)
                    ?.allKotlinSourceSets?.flatMap { it.kotlin.srcDirs }.orEmpty()
            })

            // AGP stores only this variant's unit-test execution data here.
            executionData.from(
                fileTree(
                    layout.buildDirectory.dir(
                        "outputs/unit_test_code_coverage/${variant.name}UnitTest"
                    )
                ) { include("*.exec") })
        }

        variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT).use(report).toGet(
            ScopedArtifact.CLASSES,
            VariantCoverageReport::classJars,
            VariantCoverageReport::classFolders,
        )

        if (variant.name == "debug" || variant.name == "fullDebug") {
            tasks.register("ciCoverage") {
                group = "verification"
                description = "Runs the selected Android/JVM unit tests and generates coverage."
                dependsOn(report)
            }
        }
    }
}
