package com.koaladevelopments.stickerstudio

import android.os.Bundle
import android.os.Parcelable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import com.koaladevelopments.kodevui.KODevSpace
import com.koaladevelopments.kodevui.KODevToast
import com.koaladevelopments.stickerstudio.packs.PackDetailScreen
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.koaladevelopments.kodevui.KODevTheme
import com.koaladevelopments.stickerstudio.debug.DesignSystemGallery
import com.koaladevelopments.stickerstudio.packs.InMemoryPackRepository
import com.koaladevelopments.stickerstudio.packs.PackFormScreen
import com.koaladevelopments.stickerstudio.packs.PackListScreen

private sealed interface Screen : Parcelable {
    @Parcelize data object Packs : Screen
    @Parcelize data object CreatePack : Screen
    @Parcelize data object Gallery : Screen
    @Parcelize data class Pack(val id: UUID) : Screen
    @Parcelize data class EditPack(val id: UUID) : Screen
}

class MainActivity : ComponentActivity() {
    // ponytail: in-memory sample packs until the core persistence (#3) lands.
    private val repository = InMemoryPackRepository.sample()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val systemDark = isSystemInDarkTheme()
            var dark by rememberSaveable { mutableStateOf(systemDark) }
            var screen by rememberSaveable { mutableStateOf<Screen>(Screen.Packs) }
            var toast by remember { mutableStateOf<String?>(null) }
            KODevTheme(darkTheme = dark) {
                Box(Modifier.fillMaxSize().background(KODevTheme.colors.bg).safeDrawingPadding()) {
                    // ponytail: one screen at a time, back goes up one level; a nav library when the editor flow needs a real stack.
                    val back = {
                        screen = when (val current = screen) {
                            is Screen.EditPack -> Screen.Pack(current.id)
                            else -> Screen.Packs
                        }
                    }
                    if (screen != Screen.Packs) BackHandler(onBack = back)
                    when (val current = screen) {
                        Screen.Packs -> PackListScreen(
                            repository = repository,
                            onCreate = { screen = Screen.CreatePack },
                            onOpen = { screen = Screen.Pack(it.id) },
                            onOpenGallery = if (BuildConfig.DEBUG) ({ screen = Screen.Gallery }) else null,
                        )
                        Screen.CreatePack -> PackFormScreen(repository, pack = null, onDone = back)
                        Screen.Gallery -> DesignSystemGallery(dark = dark, onToggleTheme = { dark = !dark }, onBack = back)
                        is Screen.Pack -> PackDetailScreen(
                            repository, current.id,
                            onEdit = { screen = Screen.EditPack(it.id) },
                            onAddSticker = { toast = "O editor de figurinhas chega na próxima etapa." },
                            onAddToWhatsApp = { toast = "O envio ao WhatsApp chega com a integração do core." },
                            onClose = back,
                        )
                        is Screen.EditPack -> PackFormScreen(repository, repository.packs.value.firstOrNull { it.id == current.id }, onDone = back)
                    }
                    toast?.let { text ->
                        LaunchedEffect(text) {
                            delay(4000)
                            toast = null
                        }
                        KODevToast(text, Modifier.align(Alignment.BottomCenter).padding(KODevSpace.space4))
                    }
                }
            }
        }
    }
}
