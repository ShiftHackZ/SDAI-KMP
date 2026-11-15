# Kotlin lint

Run `./gradlew kotlinLint` before submitting changes. Project Tests runs the same
task before unit tests and uploads HTML and SARIF reports from
`build/reports/kotlinLint/`, including when lint fails. Findings fail the task;
there is no baseline or automatic correction.

The task checks authored `.kt` and `.kts` files across all source sets, tests,
and Gradle build logic. It excludes generated build outputs, vendored sources,
and local Gradle, Kotlin, and Git state. Java, C++, Swift, and other languages
are outside this task's scope.

`config/detekt.yml` explicitly enables syntax-based checks for empty catch blocks,
invalid ranges, equality contracts, iterator implementations, discarded unary or
postfix expressions, and unsafe exception handling in `finally` or standard
object methods. It uses Detekt's light analysis without a platform classpath;
type-dependent checks, Android API lint, Compose stability analysis, and the
project's architectural conventions are not enforced by this task.

Detekt CLI `2.0.0-alpha.3` is pinned in the version catalog because it uses the
same Kotlin `2.3.21` compiler as the project. This is a prerelease tool. Its
standalone CLI runs in a separate JVM, isolating compiler dependencies and
avoiding a Gradle/AGP upgrade for the Detekt 2 Gradle plugin. Update the pin and
validate the enabled rules when upgrading Kotlin.

Formatting remains separate: use Android Studio's **Reformat Code** with the
checked-in project code style for Kotlin and Java. Use Prettier for supported
documentation and configuration files. The lint task does not impose a second
Kotlin formatting style.
