// Súbor: SharedComponents.kt
package com.example.op

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ==========================================================
//      KOMPONENT PRE VÝBER KANÁLA (CHANNEL)
// ==========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelDropdown(
    selectedChannel: String,
    onChannelSelected: (String) -> Unit,
    channelOptions: List<String>,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedChannel,
            onValueChange = {},
            label = { Text("Kanál") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val dropdownOptions = remember(channelOptions) {
                channelOptions.filter { it != "Všetky" }
            }

            dropdownOptions.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onChannelSelected(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

// ==========================================================
//      VLASTNÝ OUTLINEDBOX KOMPONENT
// ==========================================================
@Composable
fun OutlinedBox(
    modifier: Modifier = Modifier,
    label: String,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.extraSmall
                )
                .padding(0.dp)
        ) {
            content()
        }

        Text(
            text = label,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .padding(start = 12.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 4.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
