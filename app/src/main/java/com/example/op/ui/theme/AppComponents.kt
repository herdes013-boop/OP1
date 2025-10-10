// Nový súbor: app/src/main/java/com/example/op/ui/theme/AppComponents.kt

package com.example.op.ui.theme

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Naša vlastná verzia NavigationBar, ktorá vždy použije správnu farbu z témy
 * a vypne dodatočné tónovanie.
 */
@Composable
fun AppNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    NavigationBar(
        modifier = modifier,
        // Tu natvrdo nastavíme farbu pozadia, ktorú chceme
        containerColor = MaterialTheme.colorScheme.surface,
        // Toto vypne prídavný farebný odtieň, ktorý pridáva Material 3
        tonalElevation = 0.dp,
        content = content
    )
}
