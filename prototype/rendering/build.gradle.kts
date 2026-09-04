// Top-level build file — plugins are declared here with `apply false`
// so the actual configuration lives in module build files.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
