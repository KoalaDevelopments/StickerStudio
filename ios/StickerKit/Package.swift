// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "StickerKit",
    platforms: [.iOS(.v17), .macOS(.v14)],
    products: [
        .library(name: "StickerDomain", targets: ["StickerDomain"]),
    ],
    targets: [
        .target(name: "StickerDomain"),
        .testTarget(name: "StickerDomainTests", dependencies: ["StickerDomain"]),
    ]
)
