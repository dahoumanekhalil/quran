// Top-level build file. Plugins declared here with `apply false` so each
// module can opt in. Aligns with ADR-0019 (Gradle Kotlin DSL + version
// catalog + multi-module).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.junit5) apply false
    alias(libs.plugins.spotless)
}

// TASK-034 — Spotless with ktlint on every Kotlin source file.
// `./gradlew spotlessCheck` runs in CI (wired into `check`).
spotless {
    val ktlintVersion = libs.versions.ktlint.get()
    kotlin {
        target("**/src/**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint(ktlintVersion)
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**/*.gradle.kts")
        ktlint(ktlintVersion)
    }
}

subprojects {
    // Ensure every submodule's `check` runs Spotless via the root task.
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(rootProject.tasks.named("spotlessCheck"))
    }
}
