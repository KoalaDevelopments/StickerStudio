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
import com.koaladevelopments.stickerstudio.packs.PackListScreen

class MainActivity : ComponentActivity() {
    // ponytail: in-memory sample packs until the core persistence (#3) lands.
    private val repository = InMemoryPackRepository.sample()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val systemDark = isSystemInDarkTheme()
            var dark by rememberSaveable { mutableStateOf(systemDark) }
            var gallery by rememberSaveable { mutableStateOf(false) }
            KODevTheme(darkTheme = dark) {
                Box(Modifier.fillMaxSize().background(KODevTheme.colors.bg).safeDrawingPadding()) {
                    if (gallery) {
                        BackHandler { gallery = false }
                        DesignSystemGallery(dark = dark, onToggleTheme = { dark = !dark }, onBack = { gallery = false })
                    } else {
                        PackListScreen(
                            repository = repository,
                            onCreate = {},
                            onOpen = {},
                            onOpenGallery = if (BuildConfig.DEBUG) ({ gallery = true }) else null,
                        )
                    }
                }
            }
        }
    }
}
