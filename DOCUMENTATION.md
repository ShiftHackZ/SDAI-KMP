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

ONNX, MediaPipe, MNN, stable-diffusion.cpp and experimental PrismML Bonsai execute in
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

## MNN model bundles

The Alpha Android arm64 MNN provider runs one SD 1.5 image at 512×512,
with PNDM, CFG 7.5, 1–50 steps and CPU/OpenCL selection. It has no negative prompt,
img2img, batch, LoRA, NPU or iOS execution. Backend selection does not guarantee
that all operators execute on a GPU. Requested unavailable OpenCL fails explicitly.

Catalog downloads use `docs/mnn.json`, the existing typed model response and
[the documented catalog contract](network/mnn-catalog.openapi.json). The catalog
includes the SD 1.5 General bundle with eight-bit weights, hosted on
[Hugging Face](https://huggingface.co/moroz-ellmsha/sdai-mnn-model-archives).
Its download URL pins the exact artifact revision; the host also provides the
integrity manifest, archive SHA256, license and attribution.
Full/FOSS custom selection reuses the existing folder picker and storage
permission flow. Setup validates the complete external bundle and creates an
app-owned immutable snapshot, preserving the external source. Model deletion
waits for active native leases; queued removed/replaced identities fail explicitly.

[The packaging guide](scripts/mnn/README.md) describes pinned prebuilt graphs,
matching tokenizer preparation, licensing and the versioned integrity manifest.
Generated weights and archives remain outside Git. The runtime source is fetched
by exact revision and SHA256, with a narrow build-local SD 1.5 patch. Its common
`MnnRuntime` API has Android execution and an iOS NoOp binding. Android installs
its feature bindings only in the inference child graph.

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

[Update Dokka](.github/workflows/dokka.yml) checks `master` after pushes and
waits for a 5-minute quiet period before generation. A newer push replaces the
pending check, batching closely spaced merges into one update. Only the lightweight
Ubuntu check waits; the macOS generation job starts afterward. Pushes that change
only `docs/` are ignored, preventing generated documentation from triggering itself.

The lightweight check fingerprints Kotlin/Java sources (including KDoc), source
Markdown and Gradle configuration. Additions, changes and deletions all count;
generated HTML and vendored sources do not. Unchanged inputs skip the expensive
generation job. Each successful build records its input fingerprint in
`docs/docs/.dokka-inputs.sha256`; the first run establishes this marker.

To update **master immediately**, open **Actions → Update Dokka → Run
workflow** and select `master`. Runs dispatched from other branches are skipped.
Manual runs skip the quiet period and check for changed inputs immediately.
Enable `force` only to regenerate even when the input fingerprint matches:

```sh
gh workflow run dokka.yml --ref master
```

The workflow uses macOS so iOS source sets are available. It regenerates the full
API reference, removes obsolete pages and commits only `docs/docs/` to `master`.
Only one generation job runs at a time. If `master` advances during generation,
that result is discarded and a later check uses its latest inputs.

Updating `master` is independent of the state of `develop`, including when
`master` has extra hotfix commits. Immediately before publication, the workflow
checks whether both tips have the same full commit SHA and 0 commits ahead/behind.
Only in that case does it atomically fast-forward both branches to the same new
Dokka commit. No second commit or cherry-pick is created. If `develop` differs,
it is left untouched. If the optional update races or is rejected, the workflow
rechecks `master` and publishes there alone if its source commit is still current.
Pushes never overwrite branch history. Sync hotfix history back into `develop`
through the normal hotfix workflow when the branches have already diverged.

### Automation setup

Merge the workflow and its helper into `master` to enable automatic push-triggered
runs. Configure the repository secret `DOKKA_PUSH_TOKEN` with a dedicated
maintenance account's token, scoped to this repository with **Contents: read and
write**. The account must be authorized to push directly to the target branches
under their existing protection rules. The workflow does not change protections.

The built-in `GITHUB_TOKEN` does not bypass required PR reviews and its commits
do not trigger branch-based GitHub Pages builds. The dedicated writer allows
`master` documentation commits to trigger the existing `master` / `docs` Pages
publication. `develop` does not generate or publish documentation independently.
Generated-only pushes skip Project Tests and do not trigger another Dokka run.

### Local generation

Run Dokka from the repository root:

```bash
./gradlew dokkaGeneratePublicationHtml
```

The root Gradle build discovers all subprojects with their own build script,
applies Dokka to them, and aggregates their API documentation. Grouping projects
such as `:core` and `:feature` have no build script and are excluded. The separate
`build-logic` included build is not part of the application's API reference.

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

New modules included in `settings.gradle.kts` are documented automatically when
they have their own build script; no separate Dokka module list is maintained.

## Pre-Release Checklist

Before a release or store upload:

- Run Optimize Imports in Android Studio:
  `Project right click -> Refactor -> Optimize Imports`.
- Run `./gradlew dokkaGeneratePublicationHtml` after KDoc changes.
- Review `git diff -- docs/docs` to make sure generated documentation reflects
  the intended API changes.
- Keep unrelated generated website or screenshot changes out of documentation
  commits unless they are intentional.
