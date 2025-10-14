// Cesta: app/src/main/java/com/example/op/DetailCard.kt
package com.example.op

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Znovupoužiteľná karta pre zobrazenie detailov na obrazovkách.
 * Poskytuje jednotný vzhľad (padding, elevation, farby).
 */
@Composable
fun DetailCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // Vonkajšie odsadenie karty
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        // Vnútorný stĺpec, ktorý dostane obsah
        Column(
            modifier = Modifier.padding(16.dp) // Vnútorné odsadenie obsahu v karte
        ) {
            content()
        }
    }
}
