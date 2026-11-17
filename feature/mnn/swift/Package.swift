// swift-tools-version: 6.1
import PackageDescription

let package = Package(
  name: "SiliconDiffusionMnn",
  platforms: [.macOS(.v14), .iOS(.v17)],
  products: [.library(name: "SiliconDiffusionMnn", targets: ["SiliconDiffusionMnn"])],
  dependencies: [
    .package(url: "https://github.com/weichsel/ZIPFoundation.git", exact: "0.9.20")
  ],
  targets: [
    .binaryTarget(name: "MnnNative", path: "Artifacts/MnnNative.xcframework"),
    .target(
      name: "SiliconDiffusionMnn",
      dependencies: [
        "MnnNative", .product(name: "ZIPFoundation", package: "ZIPFoundation"),
      ], path: "Sources/SiliconDiffusionMnn",
      linkerSettings: [
        .linkedLibrary("c++"), .linkedFramework("Foundation"),
        .linkedFramework("Metal"), .linkedFramework("CoreGraphics"),
      ]),
    .testTarget(
      name: "SiliconDiffusionMnnTests", dependencies: ["SiliconDiffusionMnn"],
      path: "Tests/SiliconDiffusionMnnTests"),
  ]
)
