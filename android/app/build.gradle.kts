plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// Single source of truth for the app version. `versionCode` is derived so
// bumping `versionName` alone keeps them in lock-step and Play never rejects
// an upload for a stale monotonic code. Formula from `docs/release-cadence.md`.
val appVersionName = "0.1.0"
val appVersionCode: Int = run {
    val parts = appVersionName.split(".").map(String::toInt)
    require(parts.size == 3) { "versionName must be MAJOR.MINOR.PATCH, got '$appVersionName'" }
    (parts[0] * 10000) + (parts[1] * 100) + parts[2]
}

android {
    namespace = "app.mushaf"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.mushaf"
        minSdk = 24
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Phase 17 — release-signing scaffold. Reads from environment / gradle
    // properties so no secret ever enters git. If any variable is missing,
    // the release build stays unsigned and Play App Signing handles the
    // final signing step upstream (per ADR-0020).
    val releaseKeystorePath = System.getenv("MUSHAF_KEYSTORE_PATH") ?: providers.gradleProperty("MUSHAF_KEYSTORE_PATH").orNull
    val releaseKeystorePassword = System.getenv("MUSHAF_KEYSTORE_PASSWORD") ?: providers.gradleProperty("MUSHAF_KEYSTORE_PASSWORD").orNull
    val releaseKeyAlias = System.getenv("MUSHAF_KEY_ALIAS") ?: providers.gradleProperty("MUSHAF_KEY_ALIAS").orNull
    val releaseKeyPassword = System.getenv("MUSHAF_KEY_PASSWORD") ?: providers.gradleProperty("MUSHAF_KEY_PASSWORD").orNull
    val hasReleaseSigning = releaseKeystorePath != null &&
        releaseKeystorePassword != null &&
        releaseKeyAlias != null &&
        releaseKeyPassword != null

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            // Distinct label so debug and release installs are visually
            // distinguishable in the launcher when both are on the device.
            resValue("string", "app_name", "Mushaf (debug)")
        }
        release {
            // Phase 12 (TASK-179): R8 + resource shrinking for release.
            // Debug builds (what CI produces today) are not affected.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            resValue("string", "app_name", "Mushaf")
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:data"))
    implementation(project(":feature:reader"))
    implementation(project(":feature:navigation"))
    implementation(project(":feature:search"))
    implementation(project(":feature:settings"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
}
