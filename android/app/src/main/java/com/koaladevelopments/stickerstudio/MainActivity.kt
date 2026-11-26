package com.koaladevelopments.stickerstudio

import android.os.Bundle
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

private enum class Screen { Packs, CreatePack, Gallery }

class MainActivity : ComponentActivity() {
    // ponytail: in-memory sample packs until the core persistence (#3) lands.
    private val repository = InMemoryPackRepository.sample()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val systemDark = isSystemInDarkTheme()
            var dark by rememberSaveable { mutableStateOf(systemDark) }
            var screen by rememberSaveable { mutableStateOf(Screen.Packs) }
            KODevTheme(darkTheme = dark) {
                Box(Modifier.fillMaxSize().background(KODevTheme.colors.bg).safeDrawingPadding()) {
                    // ponytail: screen enum until the editor flow needs a back stack.
                    val back = { screen = Screen.Packs }
                    if (screen != Screen.Packs) BackHandler(onBack = back)
                    when (screen) {
                        Screen.Packs -> PackListScreen(
                            repository = repository,
                            onCreate = { screen = Screen.CreatePack },
                            onOpen = {},
                            onOpenGallery = if (BuildConfig.DEBUG) ({ screen = Screen.Gallery }) else null,
                        )
                        Screen.CreatePack -> PackFormScreen(repository, pack = null, onDone = back)
                        Screen.Gallery -> DesignSystemGallery(dark = dark, onToggleTheme = { dark = !dark }, onBack = back)
                    }
                }
            }
        }
    }
}
