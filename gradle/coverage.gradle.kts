import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

apply(plugin = "jacoco")

configure<JacocoPluginExtension> {
    toolVersion = extensions.getByType<VersionCatalogsExtension>()
        .named("libs").findVersion("jacoco").get().requiredVersion
}

val ciCoverage = tasks.register<JacocoReport>("ciCoverage") {
    group = "verification"
    description = "Runs Android/JVM unit tests across modules and aggregates JaCoCo coverage."
    reports {
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/ciCoverage/ciCoverage.xml"))
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/ciCoverage/html"))
        csv.required.set(false)
    }
}

// Variant tasks exist after Android has finalized every project's configuration.
gradle.projectsEvaluated {
    val coverageProjects = subprojects.filter { it.plugins.hasPlugin("generic.jacoco") }
    require(coverageProjects.isNotEmpty()) { "No Android/JVM coverage modules were discovered." }
    coverageProjects.forEach { module ->
        val selectedTests = module.tasks.named("ciCoverage")
        val reports = module.tasks.withType<JacocoReport>().filter {
            it.name == "jacocoDebugReport" || it.name == "jacocoFullDebugReport"
        }
        require(reports.size == 1) { "Expected one CI coverage variant in ${module.path}." }
        ciCoverage.configure {
            dependsOn(selectedTests)
            classDirectories.from(reports.map { it.classDirectories })
            sourceDirectories.from(reports.map { it.sourceDirectories })
            executionData.from(reports.map { it.executionData })
        }
    }
}
