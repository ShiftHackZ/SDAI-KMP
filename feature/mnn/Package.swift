// swift-tools-version: 6.1
import PackageDescription

let package = Package(
  name: "SiliconDiffusionMnn",
  platforms: [.iOS(.v17)],
  products: [.library(name: "SiliconDiffusionMnn", targets: ["SiliconDiffusionMnn"])],
  dependencies: [
    .package(url: "https://github.com/weichsel/ZIPFoundation.git", exact: "0.9.20")
  ],
  targets: [
    .binaryTarget(name: "MnnNative", path: "swift/Artifacts/MnnNative.xcframework"),
    .target(
      name: "SiliconDiffusionMnn",
      dependencies: [
        "MnnNative", .product(name: "ZIPFoundation", package: "ZIPFoundation"),
      ], path: "swift/Sources/SiliconDiffusionMnn",
      linkerSettings: [
        .linkedLibrary("c++"), .linkedFramework("Foundation"),
        .linkedFramework("Metal"), .linkedFramework("CoreGraphics"),
      ]),
    .testTarget(
      name: "SiliconDiffusionMnnTests", dependencies: ["SiliconDiffusionMnn"],
      path: "swift/Tests/SiliconDiffusionMnnTests"),
  ]
)
