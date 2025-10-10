package com.example.op.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default, // Toto môžeme nechať, `bodyLarge` je zvyčajne v poriadku
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    titleLarge = TextStyle(
        // ❌ TENTO RIADOK ZMAŽEME ALEBO ZAKOMENTUJEME ❌
        // fontFamily = FontFamily.Default,

        // 👇 A TU SI UPRAVÍME ZVYŠOK PODĽA SEBA 👇
        fontWeight = FontWeight.Medium, // Skús Medium, je to menej hrubé ako Bold, ale výraznejšie ako Normal
        fontSize = 21.sp,               // Pôvodne bolo 22.sp, toto je jemné zmenšenie
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
)
