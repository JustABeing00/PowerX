plugins {
    id("com.android.application")
}

android {
    namespace = "dev.powerx.browser"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.powerx.browser"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-poc"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    // Kotlin language/bytecode level follows the toolchain default.
    // (AGP 9 manages Kotlin itself; no kotlinOptions block.)
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    // Proof-of-concept engine (MPL-2.0). Pinned; see docs/THIRD_PARTY_NOTICES.md.
    // Replaceable via engine/BrowserEngine — future Chromium module plugs in here.
    // 152.x: POM-verified deps (core 1.18.0, media3 1.10.1) fit SDK 36.
    // 159.x needs SDK 37, which is not published publicly yet — revisit later.
    implementation("org.mozilla.geckoview:geckoview:152.0.20260612001812")
}
