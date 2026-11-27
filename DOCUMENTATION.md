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
[`feature/openai-oauth/`](feature/openai-oauth/); the shared `:presentation:app`
composition root registers its public Koin module. Its consumed contracts live in
[network/openai-oauth.openapi.json](network/openai-oauth.openapi.json).

When changing provider behavior, keep the README, website and FAQ, privacy
description, localized store metadata and screenshot caption sources aligned
with the implementation.

### Current upstream schema checks

Project Tests downloads the current official schemas for OpenAI, AI Horde,
Stability AI v1, Hugging Face and every Fal AI endpoint in the app catalogue.
Each provider runs as a separate matrix job with its own CI result and artifact;
one provider's failure does not cancel the others.
The checker compares consumed routes and request/response wire types with metadata
exported from the compiled application serializers. Hugging Face inference uses
its official task JSON Schema alongside the Hub OpenAPI document. ArliAI and
ChatGPT OAuth are excluded until public upstream schemas are available.

Run the same checks locally with JDK 17, the Android SDK and Python 3.10 or newer:

```sh
python3 -m venv /tmp/sdai-provider-contracts
/tmp/sdai-provider-contracts/bin/pip install -r scripts/provider_contracts/requirements.txt
./gradlew :network:exportProviderContracts --no-daemon
/tmp/sdai-provider-contracts/bin/python scripts/test_provider_contract_checker.py
/tmp/sdai-provider-contracts/bin/python scripts/check_provider_contracts.py
```

Each invocation fetches fresh upstream documents. A failed download or invalid
document fails the check; there is no bundled-spec fallback. CI uploads differences,
source URLs, fetch times and SHA-256 hashes under `upstream-provider-contracts-<provider>`.
Use `--provider OPEN_AI` (or `HORDE`, `HUGGING_FACE`, `STABILITY_AI`, `FAL_AI`) to
check one provider. Missing routes, removed consumed response fields and incompatible wire
types fail even when the application DTO field is optional. Additional upstream
response fields are allowed because the application ignores unknown keys.

Optional nullable request fields absent from an open upstream schema produce a
coverage warning: null values are omitted and the schema permits extensions.
A closed upstream schema or a published incompatible field type still fails.
Hugging Face's model-list route is an explicit coverage gap while absent from its
Hub OpenAPI document; the checker starts validating it automatically if published.
Both Hugging Face text-to-image and image-to-image task schemas are fetched.
Fal queue responses use its full queue API specification alongside model-specific
input/output schemas.

Stability's known [missing artifacts envelope](https://github.com/Stability-AI/rest-api-support/issues/38)
produces a linked warning when its upstream response is an array. Only that
envelope difference is skipped; artifact item types and other operations remain
blocking. If upstream publishes the correct object envelope, the exception no
longer applies. Coverage warnings appear in logs and the report even when CI passes.

These checks compare published structural contracts. They do not call generation
APIs or prove service behavior, authentication, value ranges or model availability.
Stability's dynamic multipart request values, Fal's catalogue validation endpoint
and Hugging Face's binary output remain outside the DTO comparison. Published
schema discrepancies require investigation before treating them as runtime bugs.
The checked-in OpenAI-compatible, OAuth and Easy Diffusion specifications still
serve as application documentation. Their former static test scripts and CI jobs
have been removed; these documents are not used as upstream test inputs.

## Presentation modules and application composition

`app/android` and `app/ios` are native hosts for platform lifecycle, permissions,
IPC and native runtime registration. `:presentation:app` owns the shared Compose
entrypoint, route composition and application dependency graph. Android starts
that graph through typed bootstrap bindings and receives typed host services;
the native Android app has no direct Koin dependency.

`:presentation:components` owns reusable UI, themes, image adapters and shared
UI services. Screen modules depend only on `:domain`, `:core:*`,
`:presentation:components` and external presentation libraries. Implementations
from `:feature:*`, data, network and storage are assembled by
`:presentation:app`. Screen ViewModels receive domain interfaces through their
local DI registrations. Shared services retain a single application binding.

| Presentation module                          | Responsibility                                                    |
| -------------------------------------------- | ----------------------------------------------------------------- |
| `app`                                        | Shared entrypoint, graph assembly and concrete route composition  |
| `components`                                 | Reusable UI and shared presentation services                      |
| `home`                                       | Drawer and home navigation, with content supplied by the app root |
| `onboarding`, `splash`                       | First-run flow and startup UI                                     |
| `settings`, `configuration`                  | Preferences, provider setup and model loading                     |
| `generation`                                 | Text-to-image, image-to-image and inpainting forms                |
| `gallery`, `history`                         | Gallery list/details and generation history                       |
| `statistics`, `benchmark`                    | Storage/network usage and local engine benchmarks                 |
| `donate`, `report`, `debug`, `logger`, `web` | Supporting screens                                                |

Source directories match `com.shifthackz.aisdv1.presentation.<module>`. A
single-screen module keeps its screen, ViewModel, state and intents in that
package, with `di` and `navigation` subpackages. Multi-screen modules use context
subpackages such as `generation.txt2img`, `generation.img2img`,
`statistics.storageusage` and `statistics.networkusage`.

Home and onboarding accept content slots; the app root supplies concrete
screens and previews. The root retains the generation ViewModels and supplies
the same image-to-image instance to inpainting. `:core:navigation` owns route
contracts independently of screen implementations.

The iOS host consumes the `AiSdPresentation` framework built by
`:presentation:app`; its Xcode integration uses
`:presentation:app:embedAndSignAppleFrameworkForXcode`. The main CI job runs Kotlin lint and
presentation architecture verification in separate steps. Run their checks
individually:

```sh
./gradlew kotlinLint
```

```sh
./gradlew checkPresentationArchitecture
```

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
in-process Core ML, Bonsai MLX and MNN execution paths; MNN cancellation waits for native return before releasing model leases.

## MNN model bundles

The Alpha MNN provider supports Android arm64 and physical iOS 17+ devices and runs one SD 1.5 image at 512×512,
with PNDM, CFG 7.5 and 1–50 steps. Android offers CPU/OpenCL; iOS offers CPU/Metal. It has no negative prompt,
img2img, batch, LoRA or NPU execution. The iOS simulator explicitly rejects inference. Backend selection does not guarantee
that all operators execute on a GPU. Requested unavailable GPU backends fail explicitly.

Catalog downloads use `docs/mnn.json`, the existing typed model response and
[the documented catalog contract](network/mnn-catalog.openapi.json). The catalog
includes the SD 1.5 General bundle with eight-bit weights, hosted on
[Hugging Face](https://huggingface.co/moroz-ellmsha/sdai-mnn-model-archives).
Its download URL pins the exact artifact revision; the host also provides the
integrity manifest, archive SHA256, license and attribution.
Android Full/FOSS custom selection reuses the existing folder picker and storage
permission flow. Setup validates the complete external bundle and creates an
app-owned immutable snapshot, preserving the external source. Model deletion
waits for active native leases; queued removed/replaced identities fail explicitly.

[The packaging guide](scripts/mnn/README.md) describes pinned prebuilt graphs,
matching tokenizer preparation, licensing and the versioned integrity manifest.
Generated weights and archives remain outside Git. The runtime source is fetched
by exact revision and SHA256, with a narrow build-local SD 1.5 patch. Its common
`MnnRuntime` API has isolated Android execution and an injected asynchronous iOS bridge. Android installs its native bindings only in the inference child graph.

The same `feature/mnn` module owns the iOS Swift package under `swift/`, with a module-root wrapper like Bonsai. Run `python3 scripts/build_mnn_ios.py` before SwiftPM resolution or Xcode builds; it generates ignored unsigned device, simulator and macOS native slices from pinned source. iOS accepts catalog models only. Its serial runtime retains immutable model leases and backup-excluded Application Support workspaces until native return, including cooperative cancellation. UIKit background execution is finite; expiration cancels the exact owner. Native crashes and memory termination remain outside recoverable errors.

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

Before generation, the workflow runs the same KDoc formatting validation as
Project Tests against the exact planned source commit. Pushes check the complete
before/after range; manual runs check changes since the last recorded Dokka
publication. Missing range history or invalid KDoc stops generation and publication,
including forced runs.

To update **master immediately**, open **Actions → Update Dokka → Run
workflow** and select `master`. Runs dispatched from other branches are skipped.
Manual runs skip the quiet period and check for changed inputs immediately.
Enable `force` only to regenerate even when the input fingerprint matches:

```sh
gh workflow run dokka.yml --ref master
```

The workflow uses macOS so iOS source sets are available. It regenerates the full
API reference, removes obsolete pages and commits only `docs/docs/` to `master`.
Only one publication job runs at a time. If `master` advances during generation,
that result is discarded and a later check uses its latest inputs.

After KDoc validation, four macOS jobs generate disjoint groups of modules in
parallel. Each job uses the same source commit and the Gradle project inventory;
new modules are assigned automatically. Successful jobs upload their complete
Dokka module outputs. A final job downloads all four artifacts and assembles the
aggregate reference without repeating module analysis or platform compilation.
Missing module outputs fail assembly rather than publishing a partial reference.
Each job keeps the 45-minute limit; publication waits for every shard to succeed.

Gradle parallel execution and build caching remain enabled for module generation.
The Gradle action persists its build cache; a separate cache preserves Kotlin/Native
toolchains and dependencies in `~/.konan`. All Android and iOS source sets remain
included. Gradle timing reports are uploaded when available as
`dokka-module-profile-<shard>` and `dokka-build-profile`.

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
./gradlew :dokkaGeneratePublicationHtml
```

The root Gradle build discovers all subprojects with their own build script,
applies Dokka to them, and aggregates their API documentation. Grouping projects
such as `:core` and `:feature` have no build script and are excluded. The separate
`build-logic` included build is not part of the application's API reference.

To reproduce one CI partition locally, run
`./gradlew :dokkaGenerateShard -PdokkaShardCount=4 -PdokkaShardIndex=0`, using
indices 0 through 3. Module outputs are written to `build/dokka-modules/`.
Once all partitions have completed, assemble their outputs without regenerating modules:

```sh
./gradlew :dokkaGeneratePublicationHtml -PdokkaAssembleOnly=true
```

The normal local generation command above generates everything automatically.

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

## Website and blog

GitHub Pages builds the site from `docs/` on `master` using Jekyll. Shared site
layouts and includes live in `docs/_layouts/` and `docs/_includes/`. The FAQ
source is `docs/_data/faq.json`, which generates both the HTML and the
`/faq.json` endpoint.

Create a lowercase, hyphenated folder in `docs/blog/` for each post. For
example, `docs/blog/my-post/BLOGPOST.md` becomes `/blog/my-post/` and appears in
the blog index and sitemap automatically.

1. Write the article in `BLOGPOST.md`. The YAML front matter needs a `title`,
   `description`, `date` in `YYYY-MM-DD` format, `category`, and `cover_alt`.
   Start body headings at level two because the template supplies the title.
2. Add a 1280 × 720 image named `header.jpg`. For different artwork, specify
   `cover`, `cover_width`, and `cover_height` in the front matter.
3. Save any distinct platform copy in `TELEGRAM.md`, `LINKEDIN.md`, or
   `DISCORD.md` beside the post. The Pages build excludes these files.

The template uses SDAI as the byline. To credit someone else, set `author`,
`author_type` (`Person` or `Organization`), and `author_url`. Set `updated`
when making a substantial revision to a published post.

To preview, use the
[GitHub Pages Jekyll setup](https://docs.github.com/en/pages/setting-up-a-github-pages-site-with-jekyll/testing-your-github-pages-site-locally-with-jekyll)
and run from the repository root:

```sh
bundle exec jekyll serve --source docs --destination "$(mktemp -d)"
```

## Pre-Release Checklist

Before a release or store upload:

- Run Optimize Imports in Android Studio:
  `Project right click -> Refactor -> Optimize Imports`.
- Run `./gradlew :dokkaGeneratePublicationHtml` after KDoc changes.
- Review `git diff -- docs/docs` to make sure generated documentation reflects
  the intended API changes.
- Keep unrelated generated website or screenshot changes out of documentation
  commits unless they are intentional.
