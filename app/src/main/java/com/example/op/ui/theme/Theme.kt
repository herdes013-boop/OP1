package com.example.op.ui.theme

// ... importy vašich farieb (Green_OP, Pink40, atď.) ...
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.CompositionLocalProvider











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
    background = Color(0xFFEAEAEA),
    surface = Color.White,

    // 🔥 kľúčové: nastav surfaceVariant na bielu
    surfaceVariant = Color.White,
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun OPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

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
    ) {
        // 🔴🔴🔴 VYMAŽTE VŠETKO ODTIETO AŽ POTIAĽTO 🔴🔴🔴
        /*
        val searchBarColors = SearchBarDefaults.colors(
            containerColor = Color.White,
            dividerColor = MaterialTheme.colorScheme.outlineVariant,
            inputFieldColors = SearchBarDefaults.inputFieldColors()
        )

        CompositionLocalProvider(
            SearchBarDefaults.LocalSearchBarColors provides searchBarColors
        ) {
            content()
        }
        */

        // ✅✅✅ NAHRADTE TO JEDINÝM RIADKOM ✅✅✅
        content()
    }
}
