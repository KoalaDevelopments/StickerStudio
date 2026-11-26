// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "StickerKit",
    platforms: [.iOS(.v17), .macOS(.v14)],
    products: [
        .library(name: "StickerDomain", targets: ["StickerDomain"]),
        .library(name: "StickerMedia", targets: ["StickerMedia"]),
    ],
    dependencies: [
        .package(url: "https://github.com/SDWebImage/libwebp-Xcode", from: "1.6.0"),
    ],
    targets: [
        .target(name: "StickerDomain"),
        .target(
            name: "StickerMedia",
            dependencies: ["StickerDomain", .product(name: "libwebp", package: "libwebp-Xcode")]
        ),
        .testTarget(name: "StickerDomainTests", dependencies: ["StickerDomain"]),
        .testTarget(name: "StickerMediaTests", dependencies: ["StickerMedia"]),
    ]
)
