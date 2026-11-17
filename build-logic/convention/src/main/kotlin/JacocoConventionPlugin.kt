import com.shifthackz.aisdv1.buildlogic.jacocoCodeCoverageReporting
import com.shifthackz.aisdv1.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension

/**
 * Instruments Android/JVM unit tests and registers variant-specific XML and HTML reports.
 *
 * @author Dmitriy Moroz
 */
@Suppress("unused")
class JacocoConventionPlugin : Plugin<Project> {

    /**
     * Uses the same JaCoCo version for test execution and report analysis.
     *
     * @param target Android application or library receiving coverage tasks.
     */
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("jacoco")
            }

            tasks.withType<Test> {
                configure<JacocoTaskExtension> {
                    isIncludeNoLocationClasses = true
                    excludes = listOf("jdk.internal.*")
                }
            }

            extensions.configure<JacocoPluginExtension> {
                toolVersion = libs.findVersion("jacoco").get().requiredVersion
            }

            jacocoCodeCoverageReporting()
        }
    }
}
