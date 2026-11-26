package com.koaladevelopments.stickerstudio.domain

import java.util.UUID

/**
 * Pack type is fixed at creation: the WhatsApp API forbids mixing static and animated stickers.
 * An enum (not a Boolean) so a future kind (e.g. audio) is a new case, not a schema rewrite.
 */
enum class PackKind { STATIC, ANIMATED }

/** An encoded file in the app's sticker storage, with the facts the encoder measured when writing it. */
data class ImageFile(
    val fileName: String,
    val byteCount: Int,
    val width: Int,
    val height: Int,
    /** Present only for animated images. */
    val animation: Animation? = null,
) {
    data class Animation(val durationMs: Int, val shortestFrameMs: Int)
}

data class Sticker(
    val id: UUID = UUID.randomUUID(),
    /** Final exportable asset (512x512 WebP). */
    val image: ImageFile,
    val emojis: List<String>,
    val accessibilityText: String? = null,
)

data class StickerPack(
    val id: UUID = UUID.randomUUID(),
    /** WhatsApp `identifier`: stable and unique. See [WhatsAppLimits.IDENTIFIER_PATTERN]. */
    val identifier: String = id.toString(),
    val name: String,
    val publisher: String,
    val kind: PackKind,
    /** 96x96 PNG shown in the WhatsApp sticker tray. Null until generated. */
    val tray: ImageFile? = null,
    val stickers: List<Sticker> = emptyList(),
)
