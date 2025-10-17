// Súbor: Dialogs.kt
package com.example.op

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Univerzálny dialóg, ktorý sa zobrazí pri pokuse o opustenie obrazovky
 * s neuloženými zmenami.
 *
 * @param onSave Akcia, ktorá sa vykoná po kliknutí na "Uložiť".
 * @param onDiscard Akcia, ktorá sa vykoná po kliknutí na "Zahodiť".
 * @param onCancel Akcia, ktorá sa vykoná po kliknutí na "Zrušiť".
 */
@Composable
fun UnsavedChangesDialog(
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Neuložené zmeny") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Prajete si uložiť zmeny pred odchodom?",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                // Horný riadok s dvoma tlačidlami
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tlačidlo ZAHODIŤ
                    Button(
                        onClick = onDiscard,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Zahodiť")
                    }
                    // Tlačidlo ULOŽIŤ
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Uložiť")
                    }
                }
                // Spodné tlačidlo ZRUŠIŤ
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Zrušiť")
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
