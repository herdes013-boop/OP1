// Súbor: TestScreen.kt
package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TestScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    // Nastavíme lištu pre obrazovku "zoznamu"
    LaunchedEffect(Unit) {
        sharedViewModel.setTopBarState(
            TopBarState(
                title = "Testovací Zoznam",
                isVisible = true,
                navigationIcon = {
                    // Tlačidlo späť nás vráti na HomeScreen
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Naspäť")
                    }
                }
            )
        )
    }

    // Zobrazíme dve tlačidlá, ktoré nás vezmú na detail
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Kliknutím prejdete na detail. Prechod bude okamžitý, bez oneskorenia a bez prebliknutia.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = { navController.navigate("test_detail/1") }) {
            Text("Otvoriť detail pre Položku 1")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { navController.navigate("test_detail/2") }) {
            Text("Otvoriť detail pre Položku 2")
        }
    }
}
