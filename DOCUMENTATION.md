# Documentation Workflow

This project uses KDoc as the source of truth and Dokka as the API reference
generator. Treat documentation changes as part of the code change: when a class,
method, constructor, property, or behavior contract changes, update the KDoc in
the same commit.

## Provider Guides

The README contains the provider/platform and capability matrices. The website
setup guide for [OpenAI-compatible image servers](docs/openai-compatible.html)
covers complete API prefixes, optional authentication, LAN access, manual model
IDs and troubleshooting. Its consumed API contract lives in
[network/openai-compatible.openapi.json](network/openai-compatible.openapi.json).

The bundled [ChatGPT OAuth guide](docs/openai-oauth.html) describes device-code
login, protected sessions and direct image generation on Android/iOS. The
`:feature:openai-oauth` Gradle module owns the provider implementation under
[`feature/openai-oauth/`](feature/openai-oauth/); app hosts register its public Koin
module. Its consumed contracts live in
[network/openai-oauth.openapi.json](network/openai-oauth.openapi.json). Static
contract gates run in Project Tests.

When changing provider behavior, keep the README, website and FAQ, privacy
description, localized store metadata and screenshot caption sources aligned
with the implementation.

## Android native inference ownership

ONNX, MediaPipe, stable-diffusion.cpp and experimental PrismML Bonsai execute in
the private `:inference` process. Foreground generation and WorkManager use the
same host gateway; WorkManager owns scheduling and notifications while the child
owns native loading and execution. Feature host bindings must remain gateway
facades. Child-only Koin modules are composed per attempt by the engine factory.

Requests capture model paths, prompts, image parameters and backend choices in
bounded app-private files. Binder carries attempt identities and sanitized
progress. The host validates a complete PNG before repository persistence.
Cancellation follows the attempt owner, with bounded child termination when the
runtime does not cooperate. Interrupted native work requires explicit retry and
is never replayed automatically. Isolation contains process-local failures; it
does not prevent device-wide memory pressure or GPU driver failures. iOS keeps
its existing Core ML and Bonsai MLX execution paths.

## UI performance diagnostics

Android provides a separate `profile` build type, derived from release with shell
profiling enabled, a debug signature and an application ID ending in `.profile`.
It installs alongside the corresponding product app. Build it with:

```sh
./gradlew :app:assembleFullProfile -PsdaiComposeTracing=true
```

Android debug and profile builds support the bounded UI recorder without an
extra Gradle property. Capture starts **off** in each new process. Open Settings,
tap the app version seven times, open Debug Menu and enable **Record UI
diagnostics**. Disable the switch to stop recording while keeping the snapshot;
Reset clears the retained observations and Share opens the platform chooser.
Capture state is process-wide, so reopening the menu reflects the current state.
No capture is uploaded automatically. Release builds and unsupported platforms
use the no-op recorder and hide capture controls.

Active recording adds work: bounded counters and events, synchronized updates,
composition commit observations and correlated Android trace sections. It can
affect the performance being measured. With capture off, per-event accumulation
and trace spans are skipped; supported builds still register bounded owners and
observe capture state. This is not a zero-overhead claim. Keep capture off for
representative frame-timing comparisons.

These optional build properties remain independent of the runtime capture switch:

| Gradle property           | Purpose                                                                                                    |
| ------------------------- | ---------------------------------------------------------------------------------------------------------- |
| `sdaiComposeTracing=true` | Includes and explicitly initializes AndroidX Compose runtime tracing in debug/profile builds for Perfetto. |
| `sdaiComposeReports=true` | Writes compiler stability/skipping reports at build time.                                                  |

Both properties default to disabled. Release builds omit the optional runtime
tracer. None of these options changes generation behavior. Captures contain
fixed destination/component/subtree/event labels, observed state revisions and
bounded counters, excluding route arguments, application state, prompts and
credentials. Navigation destinations come from `:core:navigation`; non-route
modal and helper owners use a separate fixed component set.

Use `-PsdaiComposeReports=true` to write compiler metrics and reports under each
participating module's ignored `build/compose-metrics` and `build/compose-reports`
directories. These reports describe compiler stability and skipping decisions.
Runtime counters distinguish mutation requests, observed state emissions and
completed commits of the instrumented composition wrappers, including their
initial composition. Nested composables can recompose independently of a wrapper;
these counters are not a complete count of every composable's recompositions.
They do not measure skipped compositions or frame latency. Exports report
overwritten events and evicted owners so incomplete captures remain visible.

With capture enabled, synchronous state updates, target composable sections and
image decoding also emit correlated `SDAI/` Android trace sections. Trace labels
use fixed categories and numeric owner/revision values. Decode spans cover the
decoder call on its background thread; they do not span suspension or file reads.

Use Layout Inspector for runtime recomposition/skip inspection and Perfetto with
the profile variant for composition, layout, draw, Binder, GC and frame timing.
Compose function events require an active Perfetto session with AndroidX tracing
SDK enablement; adding the build property alone does not start a recording.
Keep diagnostic-count captures separate from timing comparisons and compare
matched interactions with recording disabled. Account for intentional animations,
device refresh rate, thermal state and run variance when interpreting results.

## Shared navigation ownership

`:core:navigation` owns platform-neutral routes, fixed destination identities,
launch sources, screen router contracts, no-op routers and the in-process root
navigation controller. It depends on coroutines and has no dependency on
presentation, domain or `:core:ui`. `:core:ui` exports it with `api`, so
presentation consumes navigation types transitively. Concrete screen rendering,
host composition/DI and adapters from domain launch decisions remain in
presentation. Route arguments are never used as diagnostic labels.

## What To Document

Document declarations that explain module boundaries, public contracts, shared
KMP behavior, platform-specific `actual` behavior, side effects, and non-obvious
state transitions.

Use KDoc for:

- Classes, interfaces, objects, and data classes that are part of a module API.
- ViewModels, routers, interactors, gateways, repositories, data sources, and
  platform services.
- Top-level composables and helpers that are reused across screens.
- `expect` declarations in `commonMain` and `actual` declarations when Android
  and iOS behavior differs.
- Parameters or return values that are not obvious from the type.
- Error, cancellation, threading, persistence, or network behavior that callers
  must know about.

Avoid noisy KDoc for trivial private code whose behavior is already obvious from
the implementation. Prefer a short implementation comment inside the function
when the explanation is local and not part of the API reference.

## File-Level Guidance

Dokka renders declaration pages, not arbitrary file headers. When a file needs a
high-level explanation, document the main declaration that represents the file:

- A screen file should document the screen composable or screen state holder.
- A DI file should document the module/register function.
- A mapper file should document the mapper functions when they encode API
  quirks or compatibility rules.
- A platform file should document the `actual` implementation or factory.

If a file contains only private helpers, add KDoc only to the helpers whose
behavior would otherwise be hard to recover from the code.

## KDoc Style

Keep the first sentence short and useful. It becomes the summary in generated
Dokka pages.

Recommended shape:

```kotlin
/**
 * Loads generation history items for the gallery flow.
 *
 * The returned page is sorted from newest to oldest and contains only records
 * that have a persisted image reference.
 *
 * @param limit maximum number of records to load.
 * @param offset number of newest records to skip.
 * @return a page of gallery-ready generation results.
 *
 * @author Dmitriy Moroz
 */
class LoadGalleryPageUseCase(...)
```

Use links when the related declaration is in the codebase:

```kotlin
/**
 * Saves generated images and notifies [GalleryRouter] when the operation
 * should open gallery details.
 */
```

For KMP code:

```kotlin
/**
 * Opens an external URL using the current platform browser integration.
 *
 * Android uses an `Intent`; iOS uses the UIKit application URL opener.
 */
expect class ExternalUrlRouter
```

## Before Changing Signatures

When changing a signature or class shape:

1. Update the declaration KDoc.
2. Update `@param`, `@property`, and `@return` tags.
3. Remove documentation for deleted parameters or obsolete behavior.
4. Add platform notes when an `expect` or shared API gains different Android or
   iOS semantics.
5. Let the documentation workflow regenerate Dokka after merging, or run it
   manually before publishing release work.

## Generating API Documentation

### Automatic and manual updates

[Update Dokka](.github/workflows/dokka.yml) checks `master` and `develop` every
15 minutes. A branch must have been quiet for 10 minutes before generation
starts. Closely spaced merges are therefore included in one update. GitHub may
delay scheduled runs, so these intervals are not a delivery guarantee.

The lightweight check fingerprints Kotlin/Java sources (including KDoc), source
Markdown and Gradle configuration. Additions, changes and deletions all count;
generated HTML and vendored sources do not. Unchanged inputs skip the expensive
generation job. Each successful build records its input fingerprint in
`docs/docs/.dokka-inputs.sha256`; the first run establishes this marker.

To update **any branch immediately**, open **Actions → Update Dokka → Run
workflow**. Set `target_branch` to the branch name, or leave it empty to use the
branch selected in the workflow menu. Manual runs skip the quiet period and
check for changed inputs immediately. Enable `force` only to regenerate even
when the input fingerprint matches. For example:

```sh
gh workflow run dokka.yml --ref master -f target_branch=feature/my-feature
```

The workflow uses macOS so iOS source sets are available. It regenerates the full
API reference, removes obsolete pages and commits only `docs/docs/` to the target
branch. One generation job runs per branch at a time. If a branch advances during
generation, that result is discarded; rerun manually, or let the next scheduled
check pick up `master`/`develop`. Pushes never overwrite branch history.

### Automation setup

Merge the workflow and its helper into the default branch to enable scheduled
runs. Configure the repository secret `DOKKA_PUSH_TOKEN` with a dedicated
maintenance account's token, scoped to this repository with **Contents: read and
write**. The account must be authorized to push directly to the target branches
under their existing protection rules. The workflow does not change protections.

The built-in `GITHUB_TOKEN` does not bypass required PR reviews and its commits
do not trigger branch-based GitHub Pages builds. The dedicated writer allows
`master` documentation commits to trigger the existing `master` / `docs` Pages
publication. `develop` and manually selected feature branches are not published.
Generated-only pushes skip Project Tests. No documentation commit triggers
another Dokka run; the next scheduled check sees the matching fingerprint.

### Local generation

Run Dokka from the repository root:

```bash
./gradlew dokkaGeneratePublicationHtml
```

The root Gradle build aggregates these documented modules:

- `:app`
- `:core:common`
- `:core:imageprocessing`
- `:core:localization`
- `:core:notification`
- `:core:ui`
- `:core:validation`
- `:data`
- `:demo`
- `:domain`
- `:feature:auth`
- `:feature:benchmark`
- `:feature:onnx`
- `:feature:mediapipe`
- `:feature:work`
- `:network`
- `:presentation`
- `:storage`

The generated HTML is written to:

```text
docs/docs/
```

The `decorateDokkaHtml` Gradle task runs automatically after
`dokkaGeneratePublicationHtml`. It adds SDAI navigation links, SEO metadata, and
canonical URLs to the generated pages.

Open the generated index locally from:

```text
docs/docs/index.html
```

## Publishing Notes

The `docs/` directory is the static website root. The generated API reference is
served below `/docs/`, so `docs/docs/index.html` becomes:

```text
https://sdai.moroz.cc/docs/
```

Do not hand-edit generated files under `docs/docs/` for API text changes. Update
KDoc in source code and regenerate Dokka instead. Hand edits in generated HTML
will be overwritten by the next Dokka run.

When adding a new Gradle module that should appear in the API reference, add it
to `documentedProjects` in the root `build.gradle.kts`.

## Pre-Release Checklist

Before a release or store upload:

- Run Optimize Imports in Android Studio:
  `Project right click -> Refactor -> Optimize Imports`.
- Run `./gradlew dokkaGeneratePublicationHtml` after KDoc changes.
- Review `git diff -- docs/docs` to make sure generated documentation reflects
  the intended API changes.
- Keep unrelated generated website or screenshot changes out of documentation
  commits unless they are intentional.
