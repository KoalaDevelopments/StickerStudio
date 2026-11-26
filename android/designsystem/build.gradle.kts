// The KODev Design System for Jetpack Compose. Source of truth for every value
// here: the Design System artifact (project/tokens.json). Kept inside this repo
// for now, shaped so it can move to its own repository and serve other KODev apps.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.koaladevelopments.kodevui"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
    buildFeatures { compose = true }
}

kotlin { jvmToolchain(17) }

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
