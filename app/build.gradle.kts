// TENTO SÚBOR BOL UPRAVENÝ tak, aby obsahoval všetky potrebné pluginy a nastavenia JVM Target.

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // ✅ 1. PRIDANÝ PLUGIN: Potrebný pre podporu Compose
    // ID pluginu pre compose compiler je zabezpečený v buildFeatures { compose = true } a nemusí byť explicitne uvedený v tomto bloku
}

android {
    namespace = "com.example.op"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.op"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // ✅ 2. PRIDANÉ NASTAVENIA: Kompatibilita Javy
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // ✅ 3. PRIDANÉ NASTAVENIA: Kotlin JVM Target (oprava chyby 21)
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2") // Novšia verzia
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")
    implementation("io.coil-kt:coil-compose:2.5.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    implementation("androidx.compose.foundation:foundation-layout:1.6.5")



    // Compose BOM na zjednodušenie verzií
    implementation(platform("androidx.compose:compose-bom:2023.10.00")) // Novšia BOM
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Kľúčové dependency pre Navigáciu a ViewModel
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2") // Pre ViewModely

    // 🌟🌟🌟 CHÝBAJÚCE DEPENDENCIES PRE IKONY (OPRAVA "Unresolved reference: Visibility") 🌟🌟🌟
    // Ikony Visibility/VisibilityOff sú v material-icons-extended, nie v základnom material3 balíku.
    implementation("androidx.compose.material:material-icons-core:1.5.4")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
