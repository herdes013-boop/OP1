pluginManagement {
    // 📌 Zabezpečenie, že Compose plugin sa nájde (tieto repozitáre sú kľúčové)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Nastavenie repozitárov pre project
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Definovanie koreňového projektu (rootProject)
rootProject.name = "OP"

// Definovanie aplikačného modulu (app)
include(":app")