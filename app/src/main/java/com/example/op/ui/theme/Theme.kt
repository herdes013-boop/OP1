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
    secondary = PurpleGrey80,
    tertiary = Pink80,

    // ✅ DOPLNENÉ: Zmeň tieto hodnoty podľa seba
    background = Color(0xFF98948E), // Predvolená tmavá farba, zmeň na inú, ak chceš
    surface = Color(0xFF212121), // Farba pre spodnú lištu v tmavom režime

)

// Farebná schéma pre svetlý režim
private val LightColorScheme = lightColorScheme(
    primary = TelekomMagenta,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    // ✅ DOPLNENÉ: Zmeň tieto hodnoty podľa seba
    background = Color(0xFFF6F5F1), // Príklad: veľmi svetlá fialová/šedá
    surface = Color.White,          // Príklad: čisto biela pre spodnú lištu a karty


    /*
     Ďalšie užitočné farby na definovanie:
     onPrimary = Color.White, // Farba textu na primárnom tlačidle
     onSecondary = Color.White,
     onTertiary = Color.White,
     onBackground = Color(0xFF1C1B1F), // Farba textu na pozadí
     onSurface = Color(0xFF1C1B1F), // Farba textu na spodnej lište, kartách, atď.
    */
)

@Composable
fun OPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // ... (zvyšok funkcie zostáva bez zmeny) ...

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
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
