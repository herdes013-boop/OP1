// TENTO SÚBOR JE V KORENI PROJEKTU (PROJECT LEVEL)
plugins {
    // Základné pluginy Android a Kotlin
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false // 👈 ZMENA: Aktualizované z 1.9.0 na 1.9.10

    // Odstránený org.jetbrains.kotlin.plugin.compose, pretože by mal byť buď
    // neplatný, alebo riadený cez app/build.gradle.kts a dependencies.
}
