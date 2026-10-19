// Los plugins se declaran aquí sin aplicarse; cada módulo aplica los que usa (versiones en gradle/libs.versions.toml).
// AGP 9 trae Kotlin integrado: NO se aplica org.jetbrains.kotlin.android.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
