package com.koaladevelopments.stickerstudio.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class PackValidatorTest {
    private val tray = ImageFile("tray.png", byteCount = 51_200, width = 96, height = 96)
    private val motion = ImageFile.Animation(durationMs = 3_000, shortestFrameMs = 40)

    private fun image(bytes: Int = 20_000, side: Int = 512, animation: ImageFile.Animation? = null) =
        ImageFile("sticker.webp", bytes, side, side, animation)

    @Test
    fun limitsMatchSharedSpec() {
        // spec/ is on the test classpath, see core/domain/build.gradle.kts.
        val spec = Json.parseToJsonElement(javaClass.getResource("/whatsapp-limits.json")!!.readText()).jsonObject
        fun JsonObject.obj(key: String) = getValue(key).jsonObject
        fun JsonObject.int(key: String) = getValue(key).jsonPrimitive.int
        fun JsonObject.range(key: String) = obj(key).let { it.int("min")..it.int("max") }
        val pack = spec.obj("pack")
        val tray = spec.obj("tray")
        val sticker = spec.obj("sticker")
        val l = WhatsAppLimits

        assertEquals(spec.range("stickersPerPack"), l.STICKERS_PER_PACK)
        assertEquals(pack.range("textLength"), l.TEXT_LENGTH)
        assertEquals(pack.int("maxIdentifierLength"), l.MAX_IDENTIFIER_LENGTH)
        assertEquals(pack.getValue("identifierPattern").jsonPrimitive.content, l.IDENTIFIER_PATTERN)
        assertEquals(tray.int("dimension"), l.TRAY_DIMENSION)
        assertEquals(tray.int("maxBytes"), l.MAX_TRAY_BYTES)
        assertEquals(sticker.int("dimension"), l.STICKER_DIMENSION)
        assertEquals(sticker.obj("maxBytes").int("static"), l.maxStickerBytes(PackKind.STATIC))
        assertEquals(sticker.obj("maxBytes").int("animated"), l.maxStickerBytes(PackKind.ANIMATED))
        assertEquals(sticker.int("minFrameDurationMs"), l.MIN_FRAME_DURATION_MS)
        assertEquals(sticker.int("maxAnimationDurationMs"), l.MAX_ANIMATION_DURATION_MS)
        assertEquals(sticker.range("emojis"), l.EMOJIS_PER_STICKER)
        assertEquals(sticker.obj("maxAccessibilityTextLength").int("static"), l.maxAccessibilityTextLength(PackKind.STATIC))
        assertEquals(sticker.obj("maxAccessibilityTextLength").int("animated"), l.maxAccessibilityTextLength(PackKind.ANIMATED))
    }

    @Test
    fun packsAtTheLimitsHaveNoIssues() {
        // 64 emoji = 128 UTF-16 code units, the maximum name length.
        val still = StickerPack(
            identifier = "a".repeat(126) + "-",
            name = "😀".repeat(64), publisher = "P", kind = PackKind.STATIC, tray = tray,
            stickers = List(3) { Sticker(image = image(bytes = 102_400), emojis = listOf("😀", "😂", "🎉"), accessibilityText = "a".repeat(125)) },
        )
        assertEquals(emptyList<PackIssue>(), still.validate())

        val moving = StickerPack(
            name = "Pack", publisher = "Me", kind = PackKind.ANIMATED, tray = tray,
            stickers = List(30) {
                Sticker(
                    image = image(bytes = 512_000, animation = ImageFile.Animation(durationMs = 10_000, shortestFrameMs = 8)),
                    emojis = listOf("🎉"), accessibilityText = "a".repeat(255),
                )
            },
        )
        assertEquals(emptyList<PackIssue>(), moving.validate())
    }

    @Test
    fun brokenStaticPackReportsEveryRule() {
        val pack = StickerPack(
            identifier = "bad/id",
            name = "😀".repeat(65), publisher = "", kind = PackKind.STATIC,
            tray = ImageFile("tray.png", byteCount = 51_201, width = 100, height = 96),
            stickers = listOf(
                Sticker(image = image(bytes = 102_401, side = 511), emojis = emptyList()),
                Sticker(image = image(animation = motion), emojis = listOf("😀"), accessibilityText = "a".repeat(126)),
            ),
        )

        assertEquals(
            listOf(
                PackIssue.StickerCount(2, 3..30),
                PackIssue.NameLength(130, 1..128),
                PackIssue.PublisherLength(0, 1..128),
                PackIssue.InvalidIdentifier("bad/id"),
                PackIssue.TrayDimensions(100, 96, 96),
                PackIssue.TrayBytes(51_201, 51_200),
                PackIssue.StickerDimensions(0, 511, 511, 512),
                PackIssue.StickerBytes(0, 102_401, 102_400),
                PackIssue.EmojiCount(0, 0, 1..3),
                PackIssue.StickerKind(1, PackKind.STATIC),
                PackIssue.AccessibilityTextLength(1, 126, 125),
            ),
            pack.validate(),
        )
    }

    @Test
    fun brokenAnimatedPackReportsEveryRule() {
        val pack = StickerPack(
            identifier = "a".repeat(128),
            name = "Pack", publisher = "Me", kind = PackKind.ANIMATED,
            stickers = listOf(
                Sticker(
                    image = image(bytes = 512_001, animation = ImageFile.Animation(durationMs = 10_001, shortestFrameMs = 7)),
                    emojis = listOf("1", "2", "3", "4"), accessibilityText = "a".repeat(256),
                ),
                Sticker(image = image(), emojis = listOf("😀")),
                Sticker(image = image(animation = motion), emojis = listOf("😀")),
            ),
        )

        assertEquals(
            listOf(
                PackIssue.InvalidIdentifier("a".repeat(128)),
                PackIssue.MissingTray,
                PackIssue.StickerBytes(0, 512_001, 512_000),
                PackIssue.FrameDuration(0, 7, 8),
                PackIssue.AnimationDuration(0, 10_001, 10_000),
                PackIssue.EmojiCount(0, 4, 1..3),
                PackIssue.AccessibilityTextLength(0, 256, 255),
                PackIssue.StickerKind(1, PackKind.ANIMATED),
            ),
            pack.validate(),
        )
    }
}
