package com.koaladevelopments.stickerstudio.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Real Bitmap encoding needs a device or emulator: `./gradlew :core:media:connectedDebugAndroidTest`. */
@RunWith(AndroidJUnit4::class)
class StaticStickerEncoderTest {
    private val dir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir

    @Test
    fun encodesArtworkOnTransparent512Canvas() = runBlocking {
        // 300x150 opaque red: aspect-fit leaves transparent bands above and below.
        val source = Bitmap.createBitmap(300, 150, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.RED) }
        val file = File(dir, "artwork.webp")

        val image = StaticStickerEncoder.encode(source, file)

        assertEquals(512 to 512, image.width to image.height)
        assertEquals(file.length().toInt(), image.byteCount)
        assertTrue(image.byteCount <= 100 * 1024)
        val decoded = BitmapFactory.decodeFile(file.path)
        assertEquals(512 to 512, decoded.width to decoded.height)
        assertEquals(0, Color.alpha(decoded.getPixel(5, 5)))
        val center = decoded.getPixel(256, 256)
        assertTrue(Color.alpha(center) == 255 && Color.red(center) > 240 && Color.green(center) < 15)
    }

    @Test
    fun noiseIsSqueezedUnderTheStaticLimit() = runBlocking {
        val random = Random(42)
        val pixels = IntArray(512 * 512) { Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)) }
        val source = Bitmap.createBitmap(pixels, 512, 512, Bitmap.Config.ARGB_8888)

        val image = StaticStickerEncoder.encode(source, File(dir, "noise.webp"))

        assertTrue(image.byteCount <= 100 * 1024)
    }
}
