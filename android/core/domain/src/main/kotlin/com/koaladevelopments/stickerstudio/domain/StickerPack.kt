package com.koaladevelopments.stickerstudio.domain

import java.util.UUID

/**
 * Pack type is fixed at creation: the WhatsApp API forbids mixing static and animated stickers.
 * An enum (not a Boolean) so a future kind (e.g. audio) is a new case, not a schema rewrite.
 */
enum class PackKind { STATIC, ANIMATED }

data class Sticker(
    val id: UUID = UUID.randomUUID(),
    /** Final exportable asset (512x512 WebP), relative to the app's sticker storage. */
    val fileName: String,
    val emojis: List<String>,
    val accessibilityText: String? = null,
)

data class StickerPack(
    val id: UUID = UUID.randomUUID(),
    /** WhatsApp `identifier`: stable, unique, < 128 chars of `a-z A-Z 0-9 _ - . space`. */
    val identifier: String = id.toString(),
    val name: String,
    val publisher: String,
    val kind: PackKind,
    val stickers: List<Sticker> = emptyList(),
)
