import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.BaseExtension
import com.shifthackz.aisdv1.buildlogic.configureApplication
import com.shifthackz.aisdv1.buildlogic.configureFlavors
import com.shifthackz.aisdv1.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Configures the native Android host without adding shared UI or serialization dependencies.
 * Each host declares the platform libraries used by its activity, services and resources.
 *
 * @author Dmitriy Moroz
 */
@Suppress("unused")
class ApplicationConventionPlugin : Plugin<Project> {

    /**
     * Enables Android application compilation, build metadata and Compose hosting.
     *
     * @param target Native Android application project receiving the convention.
     * @author Dmitriy Moroz
     */
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("generic.jacoco")
            }

            extensions.configure<ApplicationExtension> {
                configureApplication(this)
                buildFeatures {
                    compose = true
                    buildConfig = true
                }
                defaultConfig.targetSdk = libs.findVersion("targetSdk").get().toString().toInt()
            }
            extensions.configure<BaseExtension> {
                configureFlavors(this)
            }
        }
    }
}
