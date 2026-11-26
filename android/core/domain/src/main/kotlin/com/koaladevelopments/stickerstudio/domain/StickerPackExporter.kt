package com.koaladevelopments.stickerstudio.domain

/**
 * What the current export path can carry. Audio stays false until WhatsApp publishes the API
 * (github.com/WhatsApp/stickers/issues/1163); then the implementation and flag change, not the UI.
 */
data class ExportCapabilities(val supportsAudio: Boolean)

interface StickerPackExporter {
    val capabilities: ExportCapabilities
    suspend fun export(pack: StickerPack)
}
