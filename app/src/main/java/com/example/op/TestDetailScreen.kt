// Súbor: TestDetailScreen.kt
package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun TestDetailScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    itemId: String // Prijmeme len ID položky
) {
    // Simulujeme, že tieto dáta už máme v nejakej pamäti (napr. vo ViewModele)
    // Toto je kľúčové - dáta sú už pripravené, nečakáme na ne.
    val preloadedData = mapOf(
        "1" to Pair("Položka 1", "Detail pre položku 1 bol zobrazený okamžite."),
        "2" to Pair("Položka 2", "Detail pre položku 2 bol zobrazený okamžite.")
    )

    // OKAMŽITE si vezmeme dáta podľa ID. Žiadny delay, žiadne čakanie.
    val itemData = preloadedData[itemId]

    // OKAMŽITE nastavíme hornú lištu s finálnym titulkom.
    // Keďže sa nič nenačítava, môžeme to urobiť hneď.
    LaunchedEffect(Unit) {
        sharedViewModel.setTopBarState(
            TopBarState(
                title = itemData?.first ?: "Detail",
                isVisible = true,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Naspäť")
                    }
                }
            )
        )
    }

    // A OKAMŽITE zobrazíme obsah.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (itemData != null) {
            Text(
                text = itemData.second,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text("Chyba: Položka s ID $itemId sa nenašla.")
        }
    }
}
