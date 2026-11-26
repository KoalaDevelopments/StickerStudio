package com.koaladevelopments.stickerstudio.media

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AtomicFile
import com.koaladevelopments.stickerstudio.domain.ImageFile
import com.koaladevelopments.stickerstudio.domain.PackKind
import com.koaladevelopments.stickerstudio.domain.WhatsAppLimits
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** Even the lowest quality is over the limit. */
class StickerTooLargeException(val bytes: Int, val max: Int) : Exception("Encoded sticker is $bytes bytes, max $max")

object StaticStickerEncoder {
    /** Highest lossy quality tried. Smaller files send faster (WhatsApp's own stickers are about 15 KB). */
    // ponytail: fixed cap, lossy only. Try lossless for flat artwork if text or outlines look soft.
    internal const val MAX_QUALITY = 80

    /**
     * Draws [source] centered and aspect-fit on a transparent 512x512 canvas and writes it to [file] as WebP,
     * at the highest quality up to [MAX_QUALITY] that fits the static sticker limit. Runs on
     * [Dispatchers.Default] and checks for cancellation between encoding attempts.
     */
    suspend fun encode(source: Bitmap, file: File): ImageFile = withContext(Dispatchers.Default) {
        val side = WhatsAppLimits.STICKER_DIMENSION
        val canvas = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
        val scale = minOf(side.toFloat() / source.width, side.toFloat() / source.height)
        val width = source.width * scale
        val height = source.height * scale
        val target = RectF((side - width) / 2, (side - height) / 2, (side + width) / 2, (side + height) / 2)
        Canvas(canvas).drawBitmap(source, null, target, Paint(Paint.FILTER_BITMAP_FLAG))

        val context = currentCoroutineContext()
        val bytes = try {
            fitWithin(WhatsAppLimits.maxStickerBytes(PackKind.STATIC), MAX_QUALITY) { quality ->
                context.ensureActive()
                ByteArrayOutputStream().also { check(canvas.compress(webp, quality, it)) }.toByteArray()
            }
        } finally {
            canvas.recycle()
        }
        AtomicFile(file).run {
            val out = startWrite()
            try {
                out.write(bytes)
                finishWrite(out)
            } catch (e: Exception) {
                failWrite(out)
                throw e
            }
        }
        ImageFile(file.name, bytes.size, side, side)
    }

    /** Tries [maxQuality] first, then binary-searches lower qualities for the highest one under [maxBytes]. */
    internal fun fitWithin(maxBytes: Int, maxQuality: Int, encode: (Int) -> ByteArray): ByteArray {
        val first = encode(maxQuality)
        if (first.size <= maxBytes) return first
        var low = 0
        var high = maxQuality - 1
        var best: ByteArray? = null
        var lastSize = first.size
        while (low <= high) {
            val quality = (low + high) / 2
            val data = encode(quality)
            if (data.size <= maxBytes) {
                best = data
                low = quality + 1
            } else {
                lastSize = data.size
                high = quality - 1
            }
        }
        return best ?: throw StickerTooLargeException(lastSize, maxBytes)
    }

    private val webp = if (Build.VERSION.SDK_INT >= 30) {
        Bitmap.CompressFormat.WEBP_LOSSY
    } else {
        @Suppress("DEPRECATION")
        Bitmap.CompressFormat.WEBP
    }
}
