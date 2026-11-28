import path from "node:path";

/** Capture families remain separate so phone UI can never silently fill tablet slots. */
export const profiles = {
  iphone: { platform: "ios", kind: "phone", targets: ["appstore", "site"] },
  "duo-outer": {
    platform: "ios",
    kind: "phone",
    targets: ["appstore-duo-outer"],
  },
  "duo-inner": {
    platform: "ios",
    kind: "foldable",
    targets: ["appstore-duo-inner"],
  },
  ipad: { platform: "ios", kind: "tablet", targets: ["appstore-ipad"] },
  phone: {
    platform: "android",
    kind: "phone",
    targets: ["fastlane", "googleplay"],
  },
  "tablet-7": {
    platform: "android",
    kind: "tablet",
    targets: ["googleplay-tablet-7"],
  },
  "tablet-10": {
    platform: "android",
    kind: "tablet",
    targets: ["googleplay-tablet-10"],
  },
};

export function captureProfile(platform, name) {
  const key = name || (platform === "ios" ? "iphone" : "phone");
  const profile = profiles[key];
  if (!profile || profile.platform !== platform) {
    throw new Error(`Invalid --form-factor ${key} for ${platform}`);
  }
  return { ...profile, key };
}

export function additionalOutputSpecs(root) {
  const apple = (key, label, width, height, folder) => ({
    label,
    width,
    height,
    type: "store",
    platform: "ios",
    profile: key,
    dir: (locale) =>
      path.join(root, "docs/screenshots/appstore", locale, folder),
    file: (slide, index) =>
      `${String(index + 1).padStart(2, "0")}-${slide.id}.png`,
  });
  const tablet = (key, folder) => ({
    label: `Google Play ${key}`,
    width: key === "tablet-7" ? 1080 : 1920,
    height: key === "tablet-7" ? 1920 : 1080,
    type: "tablet",
    platform: "android",
    profile: key,
    dir: (locale) =>
      path.join(root, "docs/screenshots/googleplay", locale, folder),
    file: (slide, index) =>
      `${String(index + 1).padStart(2, "0")}-${slide.id}.png`,
  });
  return {
    "appstore-duo-outer": apple(
      "duo-outer",
      "iPhone Duo outer",
      1398,
      2034,
      "iphone-duo-outer",
    ),
    "appstore-duo-inner": apple(
      "duo-inner",
      "iPhone Duo inner",
      2853,
      2007,
      "iphone-duo-inner",
    ),
    "appstore-ipad": apple("ipad", "iPad 13-inch", 2064, 2752, "ipad-13"),
    "googleplay-tablet-7": tablet("tablet-7", "sevenInchScreenshots"),
    "googleplay-tablet-10": tablet("tablet-10", "tenInchScreenshots"),
  };
}

/** Prevent stretched phone captures or the wrong Duo display from being exported. */
export function validateCapture(profile, size) {
  const ratio = size.width / size.height;
  const limits = {
    iphone: [0.44, 0.49],
    phone: [0.43, 0.65],
    "duo-outer": [0.68, 0.7],
    "duo-inner": [1.4, 1.44],
    ipad: [0.74, 0.76],
    "tablet-7": [0.6, 0.65],
    "tablet-10": [1.55, 1.65],
  }[profile.key];
  if (ratio < limits[0] || ratio > limits[1]) {
    throw new Error(
      `${profile.key} capture is ${size.width}x${size.height}; wrong device or orientation`,
    );
  }
}
