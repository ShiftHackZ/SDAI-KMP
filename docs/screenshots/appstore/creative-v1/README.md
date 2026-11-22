# App Store creative assets

Product-page header and search-results images for English (`en-US`), Russian
(`ru`), and Ukrainian (`uk`). These are creative assets; the existing device
screenshot deck remains separate.

| File in each locale       | Placement           | Dimensions  | Format          |
| ------------------------- | ------------------- | ----------- | --------------- |
| `product-page-header.png` | Product page header | 3840 × 1646 | Opaque sRGB PNG |
| `search-results.png`      | Search results      | 1920 × 1280 | Opaque sRGB PNG |

The artwork depicts AI image creation with a central landscape and alternate
visual treatments. Localized headlines and the main artwork occupy the central
composition; atmospheric background extends to the edges for cropping.

## Copy

| Locale | Header               | Search results            |
| ------ | -------------------- | ------------------------- |
| en-US  | Imagine. Create.     | Create AI images          |
| ru     | Воображай. Создавай. | Создавай изображения с ИИ |
| uk     | Уявляй. Створюй.     | Створюй зображення з ШІ   |

## Upload

Assign each image to its matching creative-asset placement and localization in
App Store Connect. Use its Preview tool to inspect device and orientation crops
before submission. Uploading an image does not publish it automatically.

The header art-safe rectangle is 1646 × 661 at (1097, 493). At the exported
1920 × 1280 search size, the scaled art-safe rectangle is 1084 × 515 at
(418, 382.5). Keep important content within these rectangles when editing.

Generated with built-in imagegen, followed by pixel-size and opaque sRGB PNG
export. Source generation dimensions are lower than the upload dimensions;
exports are resampled. The generation brief and localized copy are in
[prompts.json](prompts.json).

References:

- [Apple asset best practices](https://developer.apple.com/app-store/asset-best-practices/)
- [Creative asset specifications](https://developer.apple.com/help/app-store-connect/reference/app-information/creative-assets-specifications)
