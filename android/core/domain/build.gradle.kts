plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin { jvmToolchain(17) }

// The tests check WhatsAppLimits against the shared spec/whatsapp-limits.json.
sourceSets.test { resources.srcDir(rootDir.resolve("../spec")) }

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.serialization.json)
}
