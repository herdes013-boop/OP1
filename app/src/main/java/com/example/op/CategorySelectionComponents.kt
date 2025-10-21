package com.example.op

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow

/**
 * Composable, ktorý zobrazuje vybrané kategórie ako štítky (chips)
 * a po kliknutí otvorí dialóg na výber.
 * Je to ekvivalent FunctionSelector.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategorySelector(
    allCategories: List<String>,
    selectedCategories: List<String>,
    onOpenDialog: () -> Unit
) {
    // Použijeme OutlinedBox, aby to vyzeralo ako ostatné polia
    OutlinedBox(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDialog),
        label = "Kategórie"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (selectedCategories.isEmpty()) {
                    Text("Vybrať kategórie...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    FlowRow(
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp
                    ) {
                        selectedCategories.forEach { category ->
                            SuggestionChip(
                                onClick = { /* Štítok nie je klikateľný */ },
                                label = { Text(category) }
                            )
                        }
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Vybrať kategórie",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Dialógové okno, ktoré zobrazí zoznam všetkých kategórií s checkboxami.
 * Je to ekvivalent FunctionSelectionDialog.
 */
@Composable
fun CategorySelectionDialog(
    allCategories: List<String>,
    selectedCategories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    // Dočasný stav pre výber v dialógu, aby sa zmeny neprejavili, kým používateľ nestlačí "Potvrdiť"
    var tempSelected by remember { mutableStateOf(selectedCategories.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vyberte kategórie") },
        text = {
            LazyColumn {
                items(allCategories, key = { it }) { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tempSelected = if (category in tempSelected) {
                                    tempSelected - category
                                } else {
                                    tempSelected + category
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = category in tempSelected,
                            onCheckedChange = null // null, lebo celý riadok je klikateľný
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(category)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(tempSelected.toList()) }) {
                Text("Potvrdiť")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušiť")
            }
        }
    )
}
