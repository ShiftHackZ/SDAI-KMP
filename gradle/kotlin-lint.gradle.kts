import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.process.CommandLineArgumentProvider

// Run the standalone CLI to keep its Kotlin compiler isolated from the build's
// compiler and avoid requiring the Detekt 2 Gradle plugin's newer Gradle version.
val lintLibraries = extensions.getByType<VersionCatalogsExtension>().named("libs")
val kotlinLintCli by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

dependencies {
    add(
        kotlinLintCli.name,
        "dev.detekt:detekt-cli:${lintLibraries.findVersion("detekt").get()}:all"
    )
}

// Detekt uses NIO globs: **/ does not also match the repository root.
val lintIncludes = listOf("*.kt", "*.kts", "**/*.kt", "**/*.kts")
val lintExclusions = listOf(
    "build/**",
    "**/build/**",
    "vendor/**",
    "**/vendor/**",
    ".gradle*/**",
    "**/.gradle*/**",
    ".build/**",
    "**/.build/**",
    ".kotlin/**",
    "**/.kotlin/**",
    ".git/**",
    "**/.git/**",
)
val lintSources = fileTree(rootDir) {
    include(lintIncludes)
    exclude(lintExclusions)
}
val lintConfig = layout.projectDirectory.file("config/detekt.yml")
val lintReports = layout.buildDirectory.dir("reports/kotlinLint")

tasks.register<JavaExec>("kotlinLint") {
    group = "verification"
    description = "Checks authored Kotlin sources and scripts with Detekt light analysis."
    classpath = kotlinLintCli
    mainClass.set("dev.detekt.cli.Main")
    maxHeapSize = "1g"
    inputs.files(lintSources).withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(lintConfig)
    outputs.dir(lintReports)
    argumentProviders.add(CommandLineArgumentProvider {
        // Bound parser inputs to authored source roots and individual scripts.
        val inputPaths = lintSources.files.map { source ->
            val relativePath = source.relativeTo(rootDir).invariantSeparatorsPath
            val sourceRootIndex = relativePath.indexOf("/src/")
            if (sourceRootIndex >= 0) rootDir.resolve(relativePath.take(sourceRootIndex + 4))
            else source
        }.distinct().sortedBy { it.path }
        listOf("--input", inputPaths.joinToString(File.pathSeparator) { it.absolutePath })
    })
    args(
        "--includes", lintIncludes.joinToString(","),
        "--excludes", lintExclusions.joinToString(","),
        "--config", lintConfig.asFile.absolutePath,
        "--base-path", rootDir.absolutePath,
        "--language-version", "2.3",
        "--analysis-mode", "light",
        "--fail-on-severity", "Warning",
        "--report", "html:${lintReports.get().file("detekt.html").asFile}",
        "--report", "sarif:${lintReports.get().file("detekt.sarif").asFile}",
    )
}
