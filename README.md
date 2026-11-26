# Sticker Studio

Apps nativos para criar packs de figurinhas do WhatsApp. Escopo em [docs/escopo-mvp.md](docs/escopo-mvp.md).

## iOS (`ios/`)

iOS 17+, SwiftUI. O app (`StickerStudio.xcodeproj`) depende do pacote local `StickerKit`, que tem o domínio em Swift puro.

```bash
cd ios/StickerKit && swift test
xcodebuild -project ios/StickerStudio.xcodeproj -scheme StickerStudio -destination 'generic/platform=iOS Simulator' build
```

## Android (`android/`)

minSdk 26, Jetpack Compose. Módulos: `:app` e `:core:domain` (Kotlin puro).

```bash
cd android && ./gradlew assembleDebug :core:domain:test
```
