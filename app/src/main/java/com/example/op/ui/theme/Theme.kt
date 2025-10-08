package com.example.op.ui.theme

import com.example.op.ui.theme.Green_OP
import com.example.op.ui.theme.Green_OP_light
import com.example.op.ui.theme.Pink40
import com.example.op.ui.theme.Pink80
import com.example.op.ui.theme.PurpleGrey40
import com.example.op.ui.theme.PurpleGrey80
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

// ✅ Správne: Farebné schémy teraz používajú naše nové farby z externého súboru Color.kt
private val DarkColorScheme = darkColorScheme(
    primary = Green_OP_light,
    secondary = PurpleGrey80, // <-- ZMENA
    tertiary = Pink80 // <-- ZMENA
)

private val LightColorScheme = lightColorScheme(
    primary = Green_OP,
    secondary = PurpleGrey40, // <-- ZMENA
    tertiary = Pink40 // <-- ZMENA
)

@Composable
fun OPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color je funkcia pre Android 12+, ktorá prispôsobí farby tapete.
    // Pre konzistentný vzhľad je lepšie mať ju vypnutú.
    dynamicColor: Boolean = false,
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb() // Priehľadný stavový riadok
            WindowCompat.setDecorFitsSystemWindows(window, false)
            // Nastaví farbu ikon v stavovom riadku (čas, batéria) na tmavú/svetlú podľa témy
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
