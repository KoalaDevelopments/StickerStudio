package com.koaladevelopments.stickerstudio.domain

/**
 * WhatsApp third-party sticker limits (docs/escopo-mvp.md 4.1).
 * Same values as `spec/whatsapp-limits.json`; the tests fail if they drift apart.
 * Text lengths count UTF-16 code units ([String.length]), like on iOS.
 */
object WhatsAppLimits {
    val STICKERS_PER_PACK = 3..30
    /** Name and publisher. */
    val TEXT_LENGTH = 1..128
    const val MAX_IDENTIFIER_LENGTH = 127
    /** The whole identifier must match. */
    const val IDENTIFIER_PATTERN = "[A-Za-z0-9_.\\- ]+"
    const val TRAY_DIMENSION = 96
    const val MAX_TRAY_BYTES = 50 * 1024
    const val STICKER_DIMENSION = 512
    const val MIN_FRAME_DURATION_MS = 8
    const val MAX_ANIMATION_DURATION_MS = 10_000
    val EMOJIS_PER_STICKER = 1..3

    fun maxStickerBytes(kind: PackKind) = when (kind) {
        PackKind.STATIC -> 100 * 1024
        PackKind.ANIMATED -> 500 * 1024
    }

    fun maxAccessibilityTextLength(kind: PackKind) = when (kind) {
        PackKind.STATIC -> 125
        PackKind.ANIMATED -> 255
    }

    internal val identifierRegex = Regex(IDENTIFIER_PATTERN)
}

/**
 * One broken WhatsApp rule. Each case carries the actual value and the limit, so the UI can say
 * exactly what to fix. `index` is the sticker's position in [StickerPack.stickers], from 0.
 */
sealed interface PackIssue {
    data class StickerCount(val count: Int, val allowed: IntRange) : PackIssue
    data class NameLength(val length: Int, val allowed: IntRange) : PackIssue
    data class PublisherLength(val length: Int, val allowed: IntRange) : PackIssue
    data class InvalidIdentifier(val identifier: String) : PackIssue
    data object MissingTray : PackIssue
    data class TrayDimensions(val width: Int, val height: Int, val required: Int) : PackIssue
    data class TrayBytes(val bytes: Int, val max: Int) : PackIssue
    data class StickerDimensions(val index: Int, val width: Int, val height: Int, val required: Int) : PackIssue
    data class StickerBytes(val index: Int, val bytes: Int, val max: Int) : PackIssue
    /** A static image in an animated pack, or the other way around. */
    data class StickerKind(val index: Int, val expected: PackKind) : PackIssue
    data class FrameDuration(val index: Int, val ms: Int, val min: Int) : PackIssue
    data class AnimationDuration(val index: Int, val ms: Int, val max: Int) : PackIssue
    data class EmojiCount(val index: Int, val count: Int, val allowed: IntRange) : PackIssue
    data class AccessibilityTextLength(val index: Int, val length: Int, val max: Int) : PackIssue
}

/**
 * Every WhatsApp rule this pack breaks, pack-level first, then tray, then stickers in order.
 * Empty means the pack can be exported.
 */
fun StickerPack.validate(): List<PackIssue> = buildList {
    val l = WhatsAppLimits

    if (stickers.size !in l.STICKERS_PER_PACK) add(PackIssue.StickerCount(stickers.size, l.STICKERS_PER_PACK))
    if (name.length !in l.TEXT_LENGTH) add(PackIssue.NameLength(name.length, l.TEXT_LENGTH))
    if (publisher.length !in l.TEXT_LENGTH) add(PackIssue.PublisherLength(publisher.length, l.TEXT_LENGTH))
    if (identifier.length > l.MAX_IDENTIFIER_LENGTH || !l.identifierRegex.matches(identifier)) {
        add(PackIssue.InvalidIdentifier(identifier))
    }

    if (tray == null) {
        add(PackIssue.MissingTray)
    } else {
        if (tray.width != l.TRAY_DIMENSION || tray.height != l.TRAY_DIMENSION) {
            add(PackIssue.TrayDimensions(tray.width, tray.height, l.TRAY_DIMENSION))
        }
        if (tray.byteCount > l.MAX_TRAY_BYTES) add(PackIssue.TrayBytes(tray.byteCount, l.MAX_TRAY_BYTES))
    }

    stickers.forEachIndexed { index, sticker ->
        val image = sticker.image
        if (image.width != l.STICKER_DIMENSION || image.height != l.STICKER_DIMENSION) {
            add(PackIssue.StickerDimensions(index, image.width, image.height, l.STICKER_DIMENSION))
        }
        val maxBytes = l.maxStickerBytes(kind)
        if (image.byteCount > maxBytes) add(PackIssue.StickerBytes(index, image.byteCount, maxBytes))
        val animation = image.animation
        when {
            kind == PackKind.STATIC && animation == null -> Unit
            kind == PackKind.ANIMATED && animation != null -> {
                if (animation.shortestFrameMs < l.MIN_FRAME_DURATION_MS) {
                    add(PackIssue.FrameDuration(index, animation.shortestFrameMs, l.MIN_FRAME_DURATION_MS))
                }
                if (animation.durationMs > l.MAX_ANIMATION_DURATION_MS) {
                    add(PackIssue.AnimationDuration(index, animation.durationMs, l.MAX_ANIMATION_DURATION_MS))
                }
            }
            else -> add(PackIssue.StickerKind(index, kind))
        }
        if (sticker.emojis.size !in l.EMOJIS_PER_STICKER) {
            add(PackIssue.EmojiCount(index, sticker.emojis.size, l.EMOJIS_PER_STICKER))
        }
        val maxText = l.maxAccessibilityTextLength(kind)
        val text = sticker.accessibilityText
        if (text != null && text.length > maxText) add(PackIssue.AccessibilityTextLength(index, text.length, maxText))
    }
}
