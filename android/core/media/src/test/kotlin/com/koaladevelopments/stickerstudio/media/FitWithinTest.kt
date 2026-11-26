package com.koaladevelopments.stickerstudio.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FitWithinTest {
    @Test
    fun searchPicksHighestQualityThatFits() {
        val tried = mutableListOf<Int>()
        val data = StaticStickerEncoder.fitWithin(100, 80) { quality ->
            tried += quality
            ByteArray(quality + 50)
        }
        assertEquals(100, data.size)
        assertTrue(tried.first() == 80 && tried.size <= 8)

        assertEquals(80, StaticStickerEncoder.fitWithin(100, 80) { ByteArray(it) }.size)

        val error = assertThrows(StickerTooLargeException::class.java) {
            StaticStickerEncoder.fitWithin(100, 80) { ByteArray(200 + it) }
        }
        assertEquals(200 to 100, error.bytes to error.max)
    }
}
