package com.example.op

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.op.ui.theme.OPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OPTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

// Definovanie navigačných ciest (Routes)
object Routes {
    // Top-level cesty
    const val HOME_ROOT = "home_root"
    const val PASSWORDS_ROOT = "passwords_root"
    const val CONTACTS_ROOT = "contacts_root"
    const val TUTORIALS_ROOT = "tutorials_root"
    const val SETTINGS_ROOT = "settings_root"

    // Sub-cesty pre Heslá
    const val PASSWORDS_LIST = "passwords_list"
    const val ADD_PASSWORD = "add_password"
    const val EDIT_PASSWORD = "edit_password/{passwordId}"
    // OPRAVA 1: Pomocná funkcia teraz prijíma String
    fun editPassword(passwordId: String) = "edit_password/$passwordId"

    // Sub-cesty pre Kontakty
    const val CONTACTS_LIST = "contacts_list"
    const val ADD_CONTACT = "add_contact"
    const val EDIT_CONTACT = "edit_contact/{contactId}"
    fun editContact(contactId: Int) = "edit_contact/$contactId"
    const val MANAGE_CHANNELS = "manage_channels"
}

// Trieda pre definíciu položiek Bottom Navigácie
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen(Routes.HOME_ROOT, "Domov", Icons.Filled.Home)
    object Passwords : Screen(Routes.PASSWORDS_ROOT, "Heslá", Icons.Filled.Lock)
    object Contacts : Screen(Routes.CONTACTS_ROOT, "Kontakty", Icons.Filled.Person)
    object Tutorials : Screen(Routes.TUTORIALS_ROOT, "Návody", Icons.Filled.MenuBook)
    object Settings : Screen(Routes.SETTINGS_ROOT, "Nastavenia", Icons.Filled.Settings)
}

val items = listOf(
    Screen.Home,
    Screen.Passwords,
    Screen.Contacts,
    Screen.Tutorials,
    Screen.Settings
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val passwordsViewModel: PasswordsViewModel = viewModel()
    val contactsViewModel: ContactsViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME_ROOT,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.HOME_ROOT) {
                HomeScreen(navController = navController)
            }
            composable(Routes.PASSWORDS_ROOT) {
                PasswordsNavHost(viewModel = passwordsViewModel)
            }
            composable(Routes.CONTACTS_ROOT) {
                ContactsNavHost(viewModel = contactsViewModel)
            }
            composable(Routes.TUTORIALS_ROOT) {
                PlaceholderScreen(title = "Návody", description = "Tu nájdete návody na používanie aplikácie.")
            }
            composable(Routes.SETTINGS_ROOT) {
                PlaceholderScreen(title = "Nastavenia", description = "Tu nastavíte preferencie aplikácie.")
            }
        }
    }
}

@Composable
fun PasswordsNavHost(viewModel: PasswordsViewModel) {
    val nestedNavController = rememberNavController()

    NavHost(nestedNavController, startDestination = Routes.PASSWORDS_LIST) {
        composable(Routes.PASSWORDS_LIST) {
            PasswordsScreen(navController = nestedNavController, viewModel = viewModel)
        }
        composable(Routes.ADD_PASSWORD) {
            AddPasswordScreen(navController = nestedNavController, viewModel = viewModel)
        }
        // Editácia hesla
        composable(
            route = Routes.EDIT_PASSWORD,
            // OPRAVA 2: Typ argumentu je teraz StringType
            arguments = listOf(navArgument("passwordId") { type = NavType.StringType })
        ) { backStackEntry ->
            // OPRAVA 3: Získavame String a poskytujeme bezpečnú predvolenú hodnotu
            val passwordId = backStackEntry.arguments?.getString("passwordId") ?: ""
            EditPasswordScreen(navController = nestedNavController, passwordId = passwordId, viewModel = viewModel)
        }
    }
}

// ... zvyšok súboru ostáva nezmenený ...
@Composable
fun ContactsNavHost(viewModel: ContactsViewModel) {
    val nestedNavController = rememberNavController()
    NavHost(nestedNavController, startDestination = Routes.CONTACTS_LIST) {
        composable(Routes.CONTACTS_LIST) {
            ContactsScreen(
                navController = nestedNavController,
                viewModel = viewModel
            )
        }
        composable(Routes.ADD_CONTACT) {
            AddContactScreen(navController = nestedNavController, viewModel = viewModel)
        }
        composable(
            route = Routes.EDIT_CONTACT,
            arguments = listOf(navArgument("contactId") { type = NavType.IntType })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getInt("contactId") ?: 0
            EditContactScreen(
                navController = nestedNavController,
                contactId = contactId,
                viewModel = viewModel,
                onBack = { nestedNavController.popBackStack() }
            )
        }
        composable(Routes.MANAGE_CHANNELS) {
            ManageChannelsScreen(
                viewModel = viewModel,
                onBack = { nestedNavController.popBackStack() }
            )
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Vitajte v OP Správcovi",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Prejdite na Heslá, Kontakty alebo Návody/Nastavenia.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(40.dp))
        Button(onClick = { navController.navigate(Routes.PASSWORDS_ROOT) }) {
            Text("Začať s Heslami")
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
