package com.example.op.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
    /*
     Môžete tu nechať aj onPrimary = Color.White, alebo to zmazať,
     základná schéma si poradí.
    */
)

@Composable
fun OPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Odporúčam dočasne nastaviť na `false`
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // ================== ZAČIATOK KĽÚČOVÝCH ZMIEN ==================

    val view = LocalView.current
    if (!view.isInEditMode) {
        // SideEffect sa postará o to, aby sa tento kód vykonal bezpečne
        SideEffect {
            val window = (view.context as Activity).window

            // 1. Nastavíme farbu systémovej lišty na úplne priehľadnú
            window.statusBarColor = Color.Transparent.toArgb()

            // 2. Povieme systému, aby nekreslil pozadie systémových líšt
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // 3. Nastavíme farbu ikon v systémovej lište (čas, batéria)
            // Na svetlej téme budú ikony tmavé, na tmavej téme budú svetlé.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // =================== KONIEC KĽÚČOVÝCH ZMIEN ===================

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

