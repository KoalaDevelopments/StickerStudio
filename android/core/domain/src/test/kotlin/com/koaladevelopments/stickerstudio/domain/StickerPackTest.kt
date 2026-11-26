package com.koaladevelopments.stickerstudio.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class StickerPackTest {
    @Test
    fun identifierMatchesWhatsAppCharset() {
        val pack = StickerPack(name = "Meu pack", publisher = "Eu", kind = PackKind.STATIC)
        assertTrue(pack.identifier.length < 128)
        assertTrue(pack.identifier.all { it.isLetterOrDigit() || it in "_-. " })
    }
}
