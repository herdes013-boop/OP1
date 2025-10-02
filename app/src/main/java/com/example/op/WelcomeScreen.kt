package com.example.op

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Vitajte v OP (Osobné Priezvisko) Správcovi",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Tu sú bezpečne uložené vaše heslá a kontakty.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = {
                // Po stlačení prejdeme na hlavný obsah (napr. Kontakty)
                navController.navigate(Routes.CONTACTS_ROOT) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true // Odstránime WelcomeScreen z Back Stacku
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text("Začať")
        }
    }
}
