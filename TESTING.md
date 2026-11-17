# Unit tests and coverage

Run the Android/JVM unit tests and generate JaCoCo XML and HTML reports:

```sh
./gradlew clean ciCoverage --no-daemon
```

Use JDK 17 and the Android SDK configured for the project. The root task discovers
modules using the JaCoCo convention automatically. It selects `debug` for plain
modules and `fullDebug` for flavored modules, including the app. Each selected
test task runs once. An enabled module without a supported CI variant fails
configuration instead of silently disappearing from coverage.

The combined report is available at:

- XML: `build/reports/jacoco/ciCoverage/ciCoverage.xml`
- HTML: `build/reports/jacoco/ciCoverage/html/index.html`

For a single module, run its variant report task, for example:

```sh
./gradlew :domain:jacocoDebugReport
./gradlew :app:jacocoFullDebugReport
```

Module reports are under `<module>/build/reports/jacoco/<report-task>/`.

## Coverage scope

Coverage measures Android/JVM production classes, including shared Kotlin
compiled for Android. It does not measure Kotlin Native/iOS, Swift, C++, device
instrumentation tests, or other Android flavors. Unit coverage does not replace
testing normal application flows on Android and iOS.

Reports include production classes without tests. Generated Android resource and
configuration classes, Compose resource accessors and Room implementations are
excluded. Class artifacts and execution data belong to the selected variant;
unrelated build variants and dependency classes are not included.

## CI and Codecov

Project Tests runs coverage for pushes to `master` and `develop`, and pull requests
targeting either branch. Pushes containing only generated `docs/docs/**` changes
are skipped. CI retains test results and HTML/XML reports as workflow artifacts
and uploads the combined XML to [Codecov](https://app.codecov.io/gh/ShiftHackZ/SDAI-KMP)
with the `android-unit` flag.

The repository Actions secret `CODECOV_TOKEN` authenticates trusted uploads.
Public fork and Dependabot pull requests use Codecov's tokenless public-PR support;
no `pull_request_target` workflow or secret forwarding is used. Codecov must have
access to the repository through its GitHub integration to publish checks and
update PR comments.

`codecov.yml` defines components by module paths and informational project/patch
checks. Coverage changes do not block merging initially; upload failures still
fail CI. Establish branch baselines before choosing enforced coverage targets.
The README badge follows `master`; comparisons need coverage for both the PR head
and its base commit.
