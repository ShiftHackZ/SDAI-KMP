import org.gradle.api.artifacts.ProjectDependency

/** Checks every declared configuration, including platform and Android flavor contributions. */
tasks.register("checkPresentationArchitecture") {
    group = "verification"
    description =
        "Rejects presentation dependency cycles and dependencies on implementation layers."
    doLast {
        val children = rootProject.subprojects.filter { it.path.startsWith(":presentation:") }
        children.forEach { child ->
            child.fileTree("src").matching { include("**/kotlin/**/*.kt") }.forEach { source ->
                val declaredPackage = Regex("(?m)^package ([\\w.]+)")
                    .find(source.readText())?.groupValues?.get(1)
                val expectedPrefix = "com.shifthackz.aisdv1.presentation.${child.name}"
                check(
                    declaredPackage != null &&
                            (declaredPackage == expectedPrefix || declaredPackage.startsWith("$expectedPrefix."))
                ) {
                    "${source.relativeTo(rootDir)} must use its module package $expectedPrefix"
                }
                val folderPackage =
                    source.parentFile.invariantSeparatorsPath.substringAfter("/kotlin/")
                        .replace('/', '.')
                check(folderPackage == declaredPackage) {
                    "${source.relativeTo(rootDir)} folder does not match package $declaredPackage"
                }
            }
        }
        val leaves = children.filter { it.path != ":presentation:app" }
        val edges = rootProject.allprojects.associate { candidate ->
            candidate.path to candidate.configurations.flatMap { configuration ->
                configuration.dependencies.withType<ProjectDependency>()
                    .map { it.path }.filter { it != candidate.path }
            }.toSet()
        }

        fun reachable(start: String): Set<String> {
            val visited = mutableSetOf<String>()
            fun visit(path: String) {
                edges[path].orEmpty().forEach { dependency ->
                    if (visited.add(dependency)) visit(dependency)
                }
            }
            visit(start)
            return visited
        }

        fun isPermitted(path: String): Boolean = path == ":domain" ||
                path.startsWith(":core:") || path == ":presentation:components"

        leaves.forEach { leaf ->
            val forbidden = reachable(leaf.path).filterNot(::isPermitted)
            check(forbidden.isEmpty()) { "${leaf.path} reaches forbidden modules: $forbidden" }
            leaf.fileTree("src").matching {
                include("**/*.kt")
                exclude("**/*Test/**")
            }.forEach { source ->
                val contents = source.readText()
                val startupAliases = Regex(
                    "(?m)^import\\s+\\S+\\.(?:initKoin|startKoin|koinApplication)\\s+as\\s+(\\w+)"
                ).findAll(contents).map { it.groupValues[1] }.toList()
                val startupNames =
                    (listOf("initKoin", "startKoin", "koinApplication") + startupAliases)
                        .joinToString("|", transform = Regex::escape)
                check(
                    !Regex("\\b(?:$startupNames)\\s*(?:\\(|\\{)").containsMatchIn(contents)
                ) {
                    "${source.relativeTo(rootDir)} starts an application graph outside presentation:app"
                }
            }
        }
        rootProject.subprojects.filter { it.path == ":domain" || it.path.startsWith(":core:") }
            .forEach { lower ->
                check(reachable(lower.path).none { it.startsWith(":presentation:") }) {
                    "${lower.path} depends on presentation"
                }
            }
        val nativeHost = project(":app")
        check(edges[nativeHost.path].orEmpty().none {
            it.startsWith(":feature:") || it in setOf(":data", ":network", ":storage", ":demo")
        }) { "Android native host assembles implementation modules; use presentation:app" }
        nativeHost.fileTree("src").matching { include("**/*.kt") }.forEach { source ->
            check(!Regex("(?m)^import org\\.koin\\.").containsMatchIn(source.readText())) {
                "${source.relativeTo(rootDir)} imports Koin instead of the typed bootstrap"
            }
        }
        check(nativeHost.configurations.none { configuration ->
            configuration.dependencies.any { it.group?.startsWith("io.insert-koin") == true }
        }) { "Android native host declares Koin; use the typed application bootstrap" }
    }
}
