import Testing
@testable import StickerDomain

@Test func identifierMatchesWhatsAppCharset() {
    let pack = StickerPack(name: "Meu pack", publisher: "Eu", kind: .static)
    #expect(pack.identifier.count < 128)
    #expect(pack.identifier.allSatisfy { $0.isLetter || $0.isNumber || "_-. ".contains($0) })
}
