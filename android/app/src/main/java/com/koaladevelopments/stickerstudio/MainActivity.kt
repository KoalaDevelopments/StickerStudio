package com.koaladevelopments.stickerstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.koaladevelopments.kodevui.KODevTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val systemDark = isSystemInDarkTheme()
            var dark by rememberSaveable { mutableStateOf(systemDark) }
            // Temporary root while the MVP screens are built: the Design
            // System gallery, to review the :designsystem layer on a device.
            KODevTheme(darkTheme = dark) {
                DesignSystemGallery(dark = dark, onToggleTheme = { dark = !dark })
            }
        }
    }
}
