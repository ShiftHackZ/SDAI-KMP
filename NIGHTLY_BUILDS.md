# Nightly Android Builds

SDAI publishes an Android `full` flavor nightly APK from `develop` through GitHub Actions.

The public APK URL is stable:

```text
https://github.com/ShiftHackZ/SDAI-KMP/releases/download/nightly/sdai-full-nightly.apk
```

The release page is:

```text
https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/nightly
```

## What Gets Built

- Android only.
- `full` flavor only.
- The `fullDev` variant inherits release settings and dependencies. Gradle produces an unsigned APK; CI signs it with Android SDK `apksigner`.
- It installs as **SDAI Dev**, with a `DEV` launcher ribbon and the application ID `com.shifthackz.aisdv1.app.full.dev`. Stable Full, FOSS and Play installations can coexist with it.
- App-private settings, credentials and history are separate. Existing nightlies that used the stable Full package remain that installation; installing the new package does not migrate their data.
- CI versions use `<version>-dev.<UTC date>.<commit>`. The version code counts seconds since 2020-01-01 UTC, so later builds update earlier nightlies independently of stable version codes.
- The scheduled workflow runs daily and skips the build when only docs, website files, or repository metadata changed since the previous nightly.
- Manual `force` builds can publish a new artifact even when the functional-file check would skip.

The workflow does not commit generated files or changing build metadata back to the repository.

## Publication Model

Nightlies are published to one GitHub prerelease named `Android Full Nightly`, backed by the moving tag `nightly`.

The workflow force-moves the `nightly` tag to the built commit, removes obsolete uploaded assets from the nightly release, and uploads the current assets with fixed filenames using overwrite mode. The Releases page should therefore show one nightly release, and that release should contain only the latest APK and checksum assets.

The workflow does not upload separate GitHub Actions artifacts, so APK downloads are kept in the single current nightly release.

## Signing Model

The workflow signs the unsigned APK with Android SDK `apksigner` using the same release JKS as normal `full` releases, configured in the `android-release` GitHub environment. The separate package ID allows stable and nightly installations to coexist even with the same signer. Before signing, CI checks the APK's package ID and version against the requested nightly metadata. See [release signing configuration](scripts/release/README.md#configure-signing).

## Local Build

```bash
./gradlew :app:assembleFullDev
```

Local builds use `-dev.local` and a timestamp version code. Set `-PsdaiDevVersionSuffix=-dev.20261116.01234567` and `-PsdaiDevVersionCode=217036800` to supply explicit metadata. Only the Full flavor enables the `dev` build type. If local release signing properties exist, Gradle signs with that release key; otherwise the APK is unsigned, as in CI before its signing step.

Regenerate dev launcher resources with `node scripts/generate_brand_icons.mjs --dev-only` using the existing `sharp` dependency. This leaves stable launcher and store artwork untouched.

## Discord Notifications

After successfully publishing a new GitHub nightly, the workflow posts a webhook embed containing its date, version, commit, up to five recent commits, build/release links, the latest APK download link and the GitHub issue tracker. Builds skipped by the functional-file check and runs with `publish=false` do not post.

Configure `DISCORD_NIGHTLY_WEBHOOK_URL` as a secret in the `android-release` GitHub environment, pointing to the `#nightly-builds` text channel. Without the secret, publication still succeeds and the notification step reports that it was skipped. A webhook delivery failure is reported as a workflow failure after publication; the GitHub release remains available.

Give ordinary members View Channel and Read Message History, deny Send Messages, Create Public/Private Threads and Send Messages in Threads, and check that other role overrides do not grant them back. Keep Manage Webhooks restricted to maintainers. The incoming webhook can publish without a custom bot. Feedback goes to the linked issue tracker.

The APK is hosted on GitHub rather than attached to Discord because Full APKs exceed Discord's default upload limit. The fixed download URL always serves the latest nightly, including when opened from an older announcement. Mention parsing is disabled so commit subjects cannot notify roles or everyone.

## Manual Run

GitHub only exposes `workflow_dispatch` for workflow files that exist on the repository default branch. Keep `.github/workflows/nightly_android.yml` on `master`; the workflow still builds `develop` through the `target_ref` input.

Run from the GitHub UI:

1. Open `Actions`.
2. Select `Android Full Nightly`.
3. Click `Run workflow`.
4. Keep `target_ref` as `develop`.
5. Set `force` to `true` when you want a build even if no functional files changed.
6. Keep `publish` as `true` to overwrite the public nightly prerelease assets.

Run from GitHub CLI:

```bash
gh workflow run nightly_android.yml --ref master -f target_ref=develop -f force=true -f publish=true
```

After the job finishes, send the stable APK URL above to testers.

## Daily Cron

The workflow runs once per day:

```yaml
schedule:
  - cron: "37 2 * * *"
```

Scheduled workflows run from the default branch, while the checkout step uses `develop` as the default build target.

## F-Droid Safety

Nightlies use the non-version, moving `nightly` tag and the GitHub release is marked as a prerelease. It is not a stable release tag, and scheduled builds point the moving tag at the selected build target, normally `develop`.

F-Droid release automation should continue to use its normal versioned tags from `master`; do not configure F-Droid metadata to match the `nightly` tag.
