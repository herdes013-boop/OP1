package com.example.op

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    // Pri vstupe na obrazovku nastavíme správnu hornú lištu
    LaunchedEffect(Unit) {
        sharedViewModel.setTopBarState(
            TopBarState(
                title = "Testovacia obrazovka",
                isVisible = true,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Naspäť")
                    }
                },
                actions = {}
            )
        )
    }

    // Jednoduché zobrazenie textu v strede obrazovky
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Testovacia obrazovka funguje!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}
