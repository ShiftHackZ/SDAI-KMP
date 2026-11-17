import com.android.build.api.dsl.LibraryExtension
import com.shifthackz.aisdv1.buildlogic.configureCompose
import com.shifthackz.aisdv1.buildlogic.configureKotlinAndroid
import com.shifthackz.aisdv1.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import com.android.build.gradle.LibraryExtension as LegacyLibraryExtension

@Suppress("unused")
class ComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("generic.jacoco")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            val extension = extensions.getByType<LibraryExtension>()
            configureCompose(extension)

            extensions.configure<LegacyLibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = libs.findVersion("targetSdk").get().toString().toInt()
            }
        }
    }
}
