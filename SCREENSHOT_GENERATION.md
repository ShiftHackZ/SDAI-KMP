# Screenshot Generation

This project generates store, website, and README screenshots from real app
screens captured on Android emulators and iOS simulators. The marketing template
is applied after capture, so rendered store images never fall back to older
screenshots.

## Script

```bash
node scripts/generate_store_screenshots.mjs <command> [options]
```

Commands:

- `plan` prints the localized screenshot deck and expected raw screenshot files.
- `capture` drives the installed app with native XCTest on iOS or Maestro on
  Android, then saves raw app screenshots.
- `render` turns raw app screenshots into store, website, and README marketing
  assets.
- `all` runs `capture`, then `render`.

## Requirements

- Node.js.
- `rsvg-convert` from librsvg for rendering SVG templates to PNG.
- Android: Maestro for automated navigation and capture.
- Android: `adb` and a running emulator.
- For Maestro 2.6 captures, use an Android 15 (API 35) emulator. Android 16/17
  can stall text input; see [Maestro issue 2718](https://github.com/mobile-dev-inc/maestro/issues/2718).
- iOS: Xcode command line tools and a booted iOS simulator.

The capture flow assumes the app is already installed on the selected device.
After changing demo images or other compiled app resources, rebuild and reinstall
the app before running `capture`.

## Locales

Supported locales:

- `en-US`
- `ru`
- `uk`

The script maps them to Android app locales and iOS simulator languages.

## Android

Default Android package is the `full` flavor:

```bash
node scripts/generate_store_screenshots.mjs capture --platform android --locale en-US --flavor full
node scripts/generate_store_screenshots.mjs render --platform android --locale en-US --targets fastlane,googleplay
```

Useful Android options:

- `--device emulator-5554` selects an adb device.
- `--flavor playstore|full|foss` selects the package id.
- `--package com.example.app` overrides the Android package id.
- `--keep-state` keeps existing app data.
- `--prepare existing` preserves app data, skips onboarding/demo setup, and
  captures the current app state.

Render all Android locales:

```bash
node scripts/generate_store_screenshots.mjs render --platform android --locales en-US,ru,uk --targets fastlane,googleplay
```

Android outputs:

- F-Droid fastlane: `fastlane/metadata/android/<locale>/images/phoneScreenshots/`
- Google Play: `docs/screenshots/googleplay/<locale>/phoneScreenshots/`

## iOS

Capture from a booted simulator:

```bash
node scripts/generate_store_screenshots.mjs capture --platform ios --locale en-US --device <udid>
node scripts/generate_store_screenshots.mjs render --platform ios --locale en-US --targets appstore,site
```

Useful iOS options:

- `--device <udid>` is required and targets a specific simulator.
- `--bundle com.shifthackz.aisdv1.app` overrides the iOS bundle id.
- `--keep-state` keeps existing app data.
- `--prepare existing` preserves app data, skips onboarding/demo setup, and
  captures the current app state.

Render all iOS locales:

```bash
node scripts/generate_store_screenshots.mjs render --platform ios --locales en-US,ru,uk --targets appstore
```

iOS outputs:

- App Store 6.9-inch screenshots:
  `docs/screenshots/appstore/<locale>/iphone-6.9/`
- Website banners from the iOS frame, English only:
  `docs/screenshots/site/ios/en-US/`
- README screenshot rows from the English iOS App Store screenshots:
  `docs/screenshots/site/readme-row-1.png`
  `docs/screenshots/site/readme-row-2.png`

Website screenshots are intentionally generated only from iOS `en-US` output.
README rows are also intentionally generated from the mobile App Store screenshots,
not from website banners.

Generate website banners and README rows:

```bash
node scripts/generate_store_screenshots.mjs render --platform ios --locale en-US --targets appstore,site
```

## Raw Screenshots

Raw simulator captures are stored in:

```text
docs/screenshots/raw/<platform>/<locale>/
```

Expected slide files:

- `01-create.png`
- `02-controls.png`
- `03-providers.png`
- `04-gallery.png`
- `05-settings.png`
- `06-open.png`

The `05-settings` slide is an unmodified Settings view, also saved as
`settings-light.png`. Light and dark layouts are never spliced together because
theme-specific settings change row positions.

## Full Regeneration

After changing demo gallery assets:

1. Rebuild and reinstall the app on Android and iOS devices.
2. Capture all required locales.
3. Render store/site outputs from the fresh raw screenshots.

Example:

```bash
node scripts/generate_store_screenshots.mjs capture --platform android --locale en-US --flavor full
node scripts/generate_store_screenshots.mjs capture --platform android --locale ru --flavor full
node scripts/generate_store_screenshots.mjs capture --platform android --locale uk --flavor full

node scripts/generate_store_screenshots.mjs capture --platform ios --locale en-US --device <udid>
node scripts/generate_store_screenshots.mjs capture --platform ios --locale ru --device <udid>
node scripts/generate_store_screenshots.mjs capture --platform ios --locale uk --device <udid>

node scripts/generate_store_screenshots.mjs render --platform android --locales en-US,ru,uk --targets fastlane,googleplay
node scripts/generate_store_screenshots.mjs render --platform ios --locales en-US,ru,uk --targets appstore
node scripts/generate_store_screenshots.mjs render --platform ios --locale en-US --targets appstore,site
```

## Partial Regeneration

The render step always renders the full deck for the selected platform/locales.
If only gallery images changed, it is still best to recapture at least the flow
that produces:

- `01-create.png`, because it shows the generated image result.
- `04-gallery.png`, because it shows the gallery grid.

Running full capture is usually safer because the capture flow naturally creates
the generated result, fills the gallery with six images, opens details, and then
captures settings.

## Troubleshooting

- Missing raw screenshot: run `capture` for the same `--platform` and `--locale`.
- Wrong language: clear app state or omit `--keep-state`, then capture again.
- Old demo images: rebuild/reinstall the app after editing `DemoAssets.kt`.
- Empty gallery: capture in normal demo preparation mode, not `--prepare existing`.
- Android status bar is not normalized: keep `--demo-status-bar` enabled.
- Need current app state only: use `--prepare existing --keep-state`.
- Retake only Settings views in an existing deck: use `--section appearance`.
  Add `--prepare existing --keep-state` to reuse an already configured device.
  It captures the actual visible Settings layout without compositing theme variants.

## Device families and store sizes

Keep each device family's captures separate. A phone capture must never be
stretched into a tablet or unfolded-phone frame. Frames preserve the full captured display aspect ratio. Metal edges and side
buttons sit outside the capture. The renderer checks each iOS capture for its
Dynamic Island or Duo camera cutout and draws one only when the capture omits it.
Duo frames retain their hinge edge. Tablet and unfolded-device frames
use their own chrome.

| Form factor | Platform | Export target            | Export size             | Raw directory below `docs/screenshots/raw/` |
| ----------- | -------- | ------------------------ | ----------------------- | ------------------------------------------- |
| `iphone`    | iOS      | `appstore`               | 1320 × 2868             | `ios/<locale>/`                             |
| `duo-outer` | iOS      | `appstore-duo-outer`     | 1398 × 2034             | `ios/duo-outer/<locale>/`                   |
| `duo-inner` | iOS      | `appstore-duo-inner`     | 2853 × 2007             | `ios/duo-inner/<locale>/`                   |
| `ipad`      | iOS      | `appstore-ipad`          | 2064 × 2752             | `ios/ipad/<locale>/`                        |
| `phone`     | Android  | `googleplay`, `fastlane` | 1080 × 1920; 778 × 1440 | `android/<locale>/`                         |
| `tablet-7`  | Android  | `googleplay-tablet-7`    | 1080 × 1920             | `android/tablet-7/<locale>/`                |
| `tablet-10` | Android  | `googleplay-tablet-10`   | 1920 × 1080             | `android/tablet-10/<locale>/`               |

Apple also accepts the transposed Duo sizes, and 2048 × 2732 or landscape
variants for 13-inch iPad. The generator uses an unfolded landscape Duo capture
and a portrait 13-inch iPad capture. See Apple's
[screenshot specifications](https://developer.apple.com/help/app-store-connect/reference/app-information/screenshot-specifications).

Google Play has 7-inch and 10-inch tablet slots. Its large-screen guidance calls
for at least four screenshots, 1080–7680 pixels per side, 16:9 landscape or 9:16
portrait, and no additional marketing copy. Both tablet exports contain six real
app views in their captured proportions without headlines. See Google's
[preview asset guidance](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en).

Select a family for capture and rendering:

```bash
node scripts/generate_store_screenshots.mjs capture --platform ios --form-factor ipad --device <ipad-udid> --locale en-US
node scripts/generate_store_screenshots.mjs render --platform ios --form-factor ipad --locales en-US,ru,uk
node scripts/generate_store_screenshots.mjs render --platform ios --form-factor duo-inner --locales en-US,ru,uk
node scripts/generate_store_screenshots.mjs render --platform android --form-factor tablet-10 --locales en-US,ru,uk
```

Omitting `--targets` selects the targets for that family. Explicit targets must
match `--form-factor`; source dimensions are checked before export. Capture flows
are temporary files under ignored `tmp/store-screenshots/maestro/`, or a custom
`--flow-dir`. Use dedicated capture simulators/emulators: the default demo flow
clears app data and generates sample images through the app's Demo mode. These
sample images demonstrate the interface; they are not evidence of inference by
any advertised provider. The Android full flavor uses the development package
`com.shifthackz.aisdv1.app.full.dev`.

For Duo capture, select the target device in Device Hub and use Closed for the
outer display, or Open and landscape orientation for the inner display. Native
capture selects display 1 for outer and display 3 for inner; `--ios-display` can
override this. Use portrait orientation for 7-inch Android captures and landscape for 10-inch captures.

Automatic unfolded-Duo capture leaves the Demo prompt empty to keep its floating
keyboard from covering the controls used for the rest of the deck.
