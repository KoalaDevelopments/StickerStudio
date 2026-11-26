package com.koaladevelopments.stickerstudio.packs

import com.koaladevelopments.stickerstudio.domain.ImageFile
import com.koaladevelopments.stickerstudio.domain.PackKind
import com.koaladevelopments.stickerstudio.domain.Sticker
import com.koaladevelopments.stickerstudio.domain.StickerPack
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Where the screens read and write packs. The in-memory one stands in until the
 * core's persistence (#3) lands; then an adapter over it replaces it.
 */
interface PackRepository {
    val packs: StateFlow<List<StickerPack>>
    suspend fun save(pack: StickerPack)
    suspend fun delete(id: UUID)
}

class InMemoryPackRepository(initial: List<StickerPack> = emptyList()) : PackRepository {
    override val packs = MutableStateFlow(initial)

    override suspend fun save(pack: StickerPack) = packs.update { list ->
        if (list.any { it.id == pack.id }) list.map { if (it.id == pack.id) pack else it } else list + pack
    }

    override suspend fun delete(id: UUID) = packs.update { list -> list.filterNot { it.id == id } }

    companion object {
        /** Sample packs covering every list state, for development and previews. */
        fun sample(): InMemoryPackRepository {
            fun stickers(count: Int, kind: PackKind) = List(count) { i ->
                Sticker(
                    image = ImageFile(
                        "$i.webp", 40_000, 512, 512,
                        if (kind == PackKind.ANIMATED) ImageFile.Animation(2_000, 40) else null,
                    ),
                    emojis = listOf("😺"),
                )
            }
            val tray = ImageFile("tray.png", 8_000, 96, 96)
            return InMemoryPackRepository(
                listOf(
                    StickerPack(name = "Meus gatos", publisher = "Koala", kind = PackKind.STATIC, tray = tray, stickers = stickers(12, PackKind.STATIC)),
                    StickerPack(name = "Reações do trabalho", publisher = "Koala", kind = PackKind.ANIMATED, tray = tray, stickers = stickers(5, PackKind.ANIMATED)),
                    StickerPack(name = "Viagem 2026", publisher = "Koala", kind = PackKind.STATIC, stickers = stickers(2, PackKind.STATIC)),
                    StickerPack(name = "Memes da família", publisher = "Koala", kind = PackKind.STATIC, stickers = stickers(4, PackKind.STATIC)),
                ),
            )
        }
    }
}
