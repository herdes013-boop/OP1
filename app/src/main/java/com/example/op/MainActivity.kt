package com.example.op



import android.os.Bundle

import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Home

import androidx.compose.material.icons.filled.Lock

import androidx.compose.material.icons.filled.MenuBook

import androidx.compose.material.icons.filled.Person

import androidx.compose.material.icons.filled.Settings

import androidx.compose.material3.Button

import androidx.compose.material3.Icon

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.NavigationBar

import androidx.compose.material3.NavigationBarItem

import androidx.compose.material3.Scaffold

import androidx.compose.material3.Surface

import androidx.compose.material3.Text

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

// Top-level cesty pre Bottom Navigation

    const val HOME_ROOT = "home_root" // Domovská obrazovka

    const val PASSWORDS_ROOT = "passwords_root"

    const val CONTACTS_ROOT = "contacts_root"

    const val TUTORIALS_ROOT = "tutorials_root" // Návody

    const val SETTINGS_ROOT = "settings_root" // Nastavenia



// Sub-cesty pre Heslá (bezo zmeny)

    const val PASSWORDS_LIST = "passwords_list"

    const val ADD_PASSWORD = "add_password"

    const val EDIT_PASSWORD = "edit_password/{passwordId}"

    fun editPassword(passwordId: Int) = "edit_password/$passwordId"



// Sub-cesty pre Kontakty (OPRAVA: Pridané pre Editáciu/Pridanie)

    const val CONTACTS_LIST = "contacts_list"

    const val ADD_CONTACT = "add_contact"

    const val EDIT_CONTACT = "edit_contact/{contactId}"

    fun editContact(contactId: Int) = "edit_contact/$contactId" // Pomocná funkcia pre navigáciu

    const val MANAGE_CHANNELS = "manage_channels" // ✅ NOVÁ CESTA PRE SPRÁVU KANÁLOV

}



// Trieda pre definíciu položiek Bottom Navigácie (ROZŠÍRENÉ)

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {

    object Home : Screen(Routes.HOME_ROOT, "Domov", Icons.Filled.Home) // Nová položka

    object Passwords : Screen(Routes.PASSWORDS_ROOT, "Heslá", Icons.Filled.Lock)

    object Contacts : Screen(Routes.CONTACTS_ROOT, "Kontakty", Icons.Filled.Person)

    object Tutorials : Screen(Routes.TUTORIALS_ROOT, "Návody", Icons.Filled.MenuBook) // Nová položka

    object Settings : Screen(Routes.SETTINGS_ROOT, "Nastavenia", Icons.Filled.Settings) // Nová položka

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



// Odstránili sme kontrolu isWelcomeScreen, pretože lišta má byť vždy viditeľná.

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

// Aktívna by mala byť tá root destinácia, nie sub-destinácie

                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,

                        onClick = {

                            navController.navigate(screen.route) {

// Vyhýbame sa hromadeniu destinácií v back stacku

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

            startDestination = Routes.HOME_ROOT, // ✅ ŠTART NA OBRAZOVKE DOMOV

            modifier = Modifier.padding(paddingValues)

        ) {

// ✅ NOVÁ CESTA: Domovská obrazovka

            composable(Routes.HOME_ROOT) {

                HomeScreen(navController = navController)

            }



// 1. NAVIGAČNÝ GRAF PRE HESLÁ

            composable(Routes.PASSWORDS_ROOT) {

                PasswordsNavHost(viewModel = passwordsViewModel)

            }



// 2. NAVIGAČNÝ GRAF PRE KONTAKTY (OPRAVA: Volanie ContactsNavHost)

            composable(Routes.CONTACTS_ROOT) {

                ContactsNavHost(viewModel = contactsViewModel) // ✅ Použitie nového vnoreného hosta

            }



// 3. NOVÁ CESTA: Návody (jednoduchý placeholder)

            composable(Routes.TUTORIALS_ROOT) {

                PlaceholderScreen(title = "Návody", description = "Tu nájdete návody na používanie aplikácie.")

            }



// 4. NOVÁ CESTA: Nastavenia (jednoduchý placeholder)

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

// Zoznam hesiel (Hlavná obrazovka)

        composable(Routes.PASSWORDS_LIST) {

            PasswordsScreen(navController = nestedNavController, viewModel = viewModel)

        }



// Pridanie nového hesla

        composable(Routes.ADD_PASSWORD) {

            AddPasswordScreen(navController = nestedNavController, viewModel = viewModel)

        }



// Editácia hesla

        composable(

            route = Routes.EDIT_PASSWORD,

            arguments = listOf(navArgument("passwordId") { type = NavType.IntType })

        ) { backStackEntry ->

            val passwordId = backStackEntry.arguments?.getInt("passwordId") ?: 0

            EditPasswordScreen(navController = nestedNavController, passwordId = passwordId, viewModel = viewModel)

        }

    }

}



// ✅ NOVÝ VNORENÝ NAVIGAČNÝ HOST PRE KONTAKTY (OPRAVA pádu)

@Composable

fun ContactsNavHost(viewModel: ContactsViewModel) {

    val nestedNavController = rememberNavController()



    NavHost(nestedNavController, startDestination = Routes.CONTACTS_LIST) {

// Zoznam kontaktov

        composable(Routes.CONTACTS_LIST) {

// Kontaktná obrazovka teraz používa vnorený navController

            ContactsScreen(

                navController = nestedNavController,

                viewModel = viewModel

            )

        }



// Pridanie nového kontaktu

        composable(Routes.ADD_CONTACT) {

            AddContactScreen(navController = nestedNavController, viewModel = viewModel)

        }



// Editácia kontaktu

        composable(

            route = Routes.EDIT_CONTACT,

            arguments = listOf(navArgument("contactId") { type = NavType.IntType })

        ) { backStackEntry ->

            val contactId = backStackEntry.arguments?.getInt("contactId") ?: 0

            EditContactScreen(

                navController = nestedNavController,

                contactId = contactId,

                viewModel = viewModel, // viewModel by mal byť správne prepojený

                onBack = { nestedNavController.popBackStack() } // ✅ OPRAVA: Pridaný chýbajúci onBack

            )

        }



// ✅ NOVÁ CESTA: Obrazovka pre správu kanálov

        composable(Routes.MANAGE_CHANNELS) {

            ManageChannelsScreen(

                viewModel = viewModel,

                onBack = { nestedNavController.popBackStack() }

            )

        }

    }

}



// --------------------------------------------------

// POMOCNÉ OBRAZOVKY (Placeholdery)

// --------------------------------------------------



// Jednoduchá Domovská obrazovka s pôvodným obsahom WelcomeScreen

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

// Tlačidlo je v novej štruktúre Bottom Navigácie nepotrebné.

        Button(onClick = { navController.navigate(Routes.PASSWORDS_ROOT) }) {

            Text("Začať s Heslami")

        }

    }

}



// Jednoduchá obrazovka pre Návody a Nastavenia

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