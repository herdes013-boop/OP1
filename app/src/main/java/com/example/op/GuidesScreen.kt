package com.example.op

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Uistite sa, že máš tieto importy:
// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidesScreen(
    onBack: () -> Unit // Zostáva, aby sme mohli zavolať navController.popBackStack()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Návody") },
                // V Bottom Navigation zvyčajne netreba Späť, ale ponechávame ho pre univerzálnosť
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Späť"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Tu budú návody…", style = MaterialTheme.typography.bodyLarge)
        }
    }
}