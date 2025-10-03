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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
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

// Routes a Screen triedy ostávajú bez zmeny...
object Routes {
    const val HOME_ROOT = "home_root"
    const val PASSWORDS_ROOT = "passwords_root"
    const val CONTACTS_ROOT = "contacts_root"
    const val TUTORIALS_ROOT = "tutorials_root"
    const val SETTINGS_ROOT = "settings_root"
    const val PASSWORDS_LIST = "passwords_list"
    const val ADD_PASSWORD = "add_password"
    const val EDIT_PASSWORD = "edit_password/{passwordId}"
    fun editPassword(passwordId: String) = "edit_password/$passwordId"
    const val CONTACTS_LIST = "contacts_list"
    const val ADD_CONTACT = "add_contact"
    const val EDIT_CONTACT = "edit_contact/{contactId}"
    fun editContact(contactId: Int) = "edit_contact/$contactId"
    const val MANAGE_CHANNELS = "manage_channels"
    const val TUTORIALS_LIST = "tutorials_list"
    const val ADD_TUTORIAL = "add_tutorial"
    const val EDIT_TUTORIAL = "edit_tutorial/{tutorialId}"
    fun editTutorial(tutorialId: String) = "edit_tutorial/$tutorialId"
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen(Routes.HOME_ROOT, "Domov", Icons.Filled.Home)
    object Passwords : Screen(Routes.PASSWORDS_ROOT, "Heslá", Icons.Filled.Lock)
    object Contacts : Screen(Routes.CONTACTS_ROOT, "Kontakty", Icons.Filled.Person)
    object Tutorials : Screen(Routes.TUTORIALS_ROOT, "Návody", Icons.Filled.MenuBook)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Passwords,
    Screen.Contacts,
    Screen.Tutorials,
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val passwordsViewModel: PasswordsViewModel = viewModel()
    val contactsViewModel: ContactsViewModel = viewModel()
    val tutorialsViewModel: TutorialsViewModel = viewModel()

    // Hlavný Scaffold, ktorý je aktívny po celú dobu
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OP Správca") },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS_ROOT) }) {
                        Icon(Icons.Filled.AccountCircle, "Profil a Nastavenia", Modifier.size(28.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF006400),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        // NavHost, ktorý mení obsah medzi lištami
        NavHost(navController = navController, startDestination = Routes.HOME_ROOT) {
            composable(Routes.HOME_ROOT) {
                HomeScreen(modifier = Modifier.padding(paddingValues))
            }
            composable(Routes.PASSWORDS_ROOT) {
                PasswordsNavHost(viewModel = passwordsViewModel, paddingValues = paddingValues)
            }
            composable(Routes.CONTACTS_ROOT) {
                ContactsNavHost(viewModel = contactsViewModel, paddingValues = paddingValues)
            }
            composable(Routes.TUTORIALS_ROOT) {
                // TutorialsNavHost teraz dostáva padding z hlavného Scaffold-u
                TutorialsNavHost(viewModel = tutorialsViewModel, paddingValues = paddingValues)
            }
            composable(Routes.SETTINGS_ROOT) {
                ProfileScreen(navController = navController, modifier = Modifier.padding(paddingValues))
            }
        }
    }
}

// === ZMENENÁ ČASŤ ===
// Táto funkcia teraz správne manažuje, kedy zobraziť zoznam a kedy editor
@Composable
fun TutorialsNavHost(viewModel: TutorialsViewModel, paddingValues: PaddingValues) {
    val nestedNavController = rememberNavController()

    NavHost(nestedNavController, startDestination = Routes.TUTORIALS_LIST) {
        composable(Routes.TUTORIALS_LIST) {
            // TutorialsScreen (zoznam) používa padding z hlavného Scaffoldu
            TutorialsScreen(
                navController = nestedNavController,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }
        composable(Routes.ADD_TUTORIAL) {
            // AddTutorialScreen (editor) si padding rieši sám vo svojom vnútornom Scaffolde
            AddTutorialScreen(navController = nestedNavController, viewModel = viewModel)
        }
    }
}

// Zvyšok MainActivity.kt ostáva rovnaký
@Composable
fun ProfileScreen(navController: NavController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Profil a Nastavenia", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("Tu sa budú nachádzať nastavenia aplikácie, pomocník a ďalšie možnosti.")
        Spacer(Modifier.height(24.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Naspäť")
        }
    }
}

@Composable
fun PasswordsNavHost(viewModel: PasswordsViewModel, paddingValues: PaddingValues) {
    val nestedNavController = rememberNavController()
    NavHost(nestedNavController, startDestination = Routes.PASSWORDS_LIST) {
        composable(Routes.PASSWORDS_LIST) {
            PasswordsScreen(
                navController = nestedNavController,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun ContactsNavHost(viewModel: ContactsViewModel, paddingValues: PaddingValues) {
    val nestedNavController = rememberNavController()
    NavHost(nestedNavController, startDestination = Routes.CONTACTS_LIST) {
        composable(Routes.CONTACTS_LIST) {
            ContactsScreen(
                navController = nestedNavController,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
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
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.VerifiedUser, "Ikona bezpečnosti", Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp))
        Text("Vitajte v OP Správcovi", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("Vaše heslá, kontakty a návody sú bezpečne na jednom mieste. Použite spodnú lištu na navigáciu.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}
