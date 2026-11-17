package com.shifthackz.aisdv1.buildlogic

import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Classpath
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Reads the Android variant's own classes through AGP's scoped artifacts API.
 * Dependencies and other build variants never enter the coverage denominator.
 * Only generated Android resources/configuration, Compose resources and Room implementations
 * are excluded; production classes without tests remain visible as missed coverage.
 *
 * @author Dmitriy Moroz
 */
abstract class VariantCoverageReport : JacocoReport() {

    /** Production class jars supplied by AGP, excluding project and external dependencies. */
    @get:Classpath
    abstract val classJars: ListProperty<RegularFile>

    /** Production class directories supplied by AGP for this variant. */
    @get:Classpath
    abstract val classFolders: ListProperty<Directory>

    init {
        val exclusions = listOf(
            "**/R.class",
            "**/R\$*.class",
            "**/BuildConfig.class",
            "**/Manifest.class",
            "**/Manifest\$*.class",
            "**/generated/resources/**",
            "**/*_Impl.class",
            "**/*_Impl\$*.class",
        )
        classDirectories.from(classFolders.map { folders ->
            folders.map { it.asFileTree.matching { exclude(exclusions) } }
        })
        classDirectories.from(classJars.map { jars ->
            jars.map { project.zipTree(it).matching { exclude(exclusions) } }
        })
    }
}
