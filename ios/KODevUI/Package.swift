// swift-tools-version: 6.0
import PackageDescription

// The KODev Design System for SwiftUI. Source of truth for every value here:
// the Design System artifact (project/tokens.json). Kept inside this repo for
// now, shaped so it can move to its own repository and serve other KODev apps.
let package = Package(
    name: "KODevUI",
    platforms: [.iOS(.v17), .macOS(.v14)],
    products: [
        .library(name: "KODevUI", targets: ["KODevUI"]),
    ],
    targets: [
        .target(name: "KODevUI", resources: [.process("Resources")]),
    ]
)
