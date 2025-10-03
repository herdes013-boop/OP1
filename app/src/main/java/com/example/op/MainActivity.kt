package com.example.op

import TutorialsScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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

object Routes {
    const val HOME_ROOT = "home_root"
    const val PASSWORDS_ROOT = "passwords_root"
    const val CONTACTS_ROOT = "contacts_root"
    const val TUTORIALS_ROOT = "tutorials_root"
    const val SETTINGS_ROOT = "settings_root"

    // --- HESLÁ ---
    const val PASSWORDS_LIST = "passwords_list"
    const val ADD_PASSWORD = "add_password"
    const val PASSWORD_DETAIL = "password_detail/{passwordId}"
    const val EDIT_PASSWORD = "edit_password/{passwordId}"
    fun passwordDetail(passwordId: String) = "password_detail/$passwordId"
    fun editPassword(passwordId: String) = "edit_password/$passwordId"

    // --- KONTAKTY ---
    const val CONTACTS_LIST = "contacts_list"
    const val ADD_CONTACT = "add_contact"
    const val EDIT_CONTACT = "edit_contact/{contactId}"
    fun editContact(contactId: Int) = "edit_contact/$contactId"
    const val MANAGE_CHANNELS = "manage_channels"

    // --- NÁVODY ---
    const val TUTORIALS_LIST = "tutorials_list"
    const val TUTORIAL_DETAIL = "tutorial_detail/{tutorialId}"
    fun tutorialDetail(tutorialId: String) = "tutorial_detail/$tutorialId"
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
    val sharedViewModel: SharedViewModel = viewModel()
    val passwordsViewModel: PasswordsViewModel = viewModel()
    val contactsViewModel: ContactsViewModel = viewModel()
    val tutorialsViewModel: TutorialsViewModel = viewModel()

    val topBarState by sharedViewModel.topBarState.collectAsState()
    val showBottomBar by sharedViewModel.showBottomBar.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            Routes.HOME_ROOT, Routes.PASSWORDS_ROOT, Routes.CONTACTS_ROOT -> {
                sharedViewModel.setShowBottomBar(true)
                val title = when (currentRoute) {
                    Routes.PASSWORDS_ROOT -> "Heslá"
                    Routes.CONTACTS_ROOT -> "Kontakty"
                    else -> "Domov"
                }
                sharedViewModel.setTopBarState(TopBarState(title = title, isVisible = true))
            }
            Routes.TUTORIALS_ROOT -> {
                sharedViewModel.setShowBottomBar(true)
                sharedViewModel.setTopBarState(TopBarState(title = "Návody", isVisible = true))
            }
        }
    }

    Scaffold(
        topBar = {
            if (topBarState.isVisible) {
                TopAppBar(
                    title = { Text(topBarState.title) },
                    navigationIcon = { topBarState.navigationIcon?.invoke() },
                    actions = { topBarState.actions?.invoke() },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF006400),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
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
        }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = Routes.HOME_ROOT) {
            composable(Routes.HOME_ROOT) {
                HomeScreen(modifier = Modifier.padding(paddingValues))
            }
            composable(Routes.PASSWORDS_ROOT) {
                PasswordsNavHost(
                    viewModel = passwordsViewModel,
                    paddingValues = paddingValues,
                    sharedViewModel = sharedViewModel
                )
            }
            composable(Routes.CONTACTS_ROOT) {
                ContactsNavHost(
                    viewModel = contactsViewModel,
                    paddingValues = paddingValues,
                    sharedViewModel = sharedViewModel
                )
            }


            composable(Routes.TUTORIALS_ROOT) {
                TutorialsNavHost(
                    tutorialsViewModel = tutorialsViewModel,
                    sharedViewModel = sharedViewModel,
                    paddingValues = paddingValues
                )
            }
            composable(Routes.SETTINGS_ROOT) {
                ProfileScreen(navController = navController, modifier = Modifier.padding(paddingValues))
            }
        }
    }
}

@Composable
fun PasswordsNavHost(viewModel: PasswordsViewModel, paddingValues: PaddingValues, sharedViewModel: SharedViewModel) {
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        val currentRoute = navBackStackEntry?.destination?.route
        val isSubScreen = currentRoute != Routes.PASSWORDS_LIST
        sharedViewModel.setShowBottomBar(!isSubScreen)

        // Pre Heslá si každá obrazovka riadi TopBar sama alebo ju neriadi vôbec
        // (Add/Edit má vlastný, Detail má vlastný, Zoznam používa globálny)
        // Už to nemusíme explicitne nastavovať tu.
    }

    NavHost(
        navController = nestedNavController,
        startDestination = Routes.PASSWORDS_LIST,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Routes.PASSWORDS_LIST) {
            PasswordsScreen(
                navController = nestedNavController,
                viewModel = viewModel
            )
        }
        composable(
            route = Routes.PASSWORD_DETAIL,
            arguments = listOf(navArgument("passwordId") { type = NavType.StringType })
        ) { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getString("passwordId")
            if (passwordId != null) {
                PasswordDetailScreen(
                    passwordId = passwordId,
                    viewModel = viewModel,
                    sharedViewModel = sharedViewModel, // <-- PRIDAJTE TOTO
                    onNavigateToEdit = { nestedNavController.navigate(Routes.editPassword(it)) },
                    onBack = { nestedNavController.popBackStack() }
                )
            }
        }
        composable(Routes.ADD_PASSWORD) {
            AddEditPasswordScreen(
                navController = nestedNavController,
                viewModel = viewModel
            )
        }
        composable(
            route = Routes.EDIT_PASSWORD,
            arguments = listOf(navArgument("passwordId") { type = NavType.StringType })
        ) { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getString("passwordId")
            AddEditPasswordScreen(
                navController = nestedNavController,
                viewModel = viewModel,
                passwordId = passwordId
            )
        }
    }
}

@Composable
fun ContactsNavHost(
    viewModel: ContactsViewModel,
    paddingValues: PaddingValues,
    // --- KROK 1: Pridajte tento parameter ---
    sharedViewModel: SharedViewModel
) {
    val nestedNavController = rememberNavController()
    // --- KROK 2: Pridajte celý tento blok ---
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        val currentRoute = navBackStackEntry?.destination?.route
        val isSubScreen = currentRoute != Routes.CONTACTS_LIST

        // Skryjeme spodnú lištu, ak nie sme na zozname
        sharedViewModel.setShowBottomBar(!isSubScreen)

        val isAddScreen = currentRoute == Routes.ADD_CONTACT

        if (isAddScreen) {
            // Pri pridaní schováme hornú lištu
            sharedViewModel.setTopBarState(TopBarState(isVisible = false))
        } else if (!isSubScreen) {
            // Ak sme na hlavnom zozname, nastavíme titulok "Kontakty"
            sharedViewModel.setTopBarState(TopBarState(title = "Kontakty", isVisible = true))
        }
        // Pre detail (editáciu) nerobíme nič, necháme to na EditContactScreen
    }
    // --- Koniec nového bloku ---

    NavHost(nestedNavController, startDestination = Routes.CONTACTS_LIST, modifier = Modifier.padding(paddingValues)) {
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
                sharedViewModel = sharedViewModel, // Teraz je to správne
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
fun TutorialsNavHost(
    tutorialsViewModel: TutorialsViewModel,
    sharedViewModel: SharedViewModel,
    paddingValues: PaddingValues // Prijíma padding z MainScreen
) {
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        val currentRoute = navBackStackEntry?.destination?.route
        val isAddOrEditScreen = currentRoute == Routes.ADD_TUTORIAL || currentRoute?.startsWith("edit_tutorial/") == true

        // Skryjeme spodnú lištu, ak nie sme na hlavnom zozname
        sharedViewModel.setShowBottomBar(currentRoute == Routes.TUTORIALS_LIST)

        // Skryjeme hornú lištu len pre Add/Edit, inak ju necháme viditeľnú
        if (isAddOrEditScreen) {
            sharedViewModel.setTopBarState(TopBarState(isVisible = false))
        } else {
            // Pre zoznam a detail sa názov nastaví v MainScreen, tu len zaistíme viditeľnosť
            sharedViewModel.setTopBarState(TopBarState(title = "Návody", isVisible = true))
        }
    }

    // Tento NavHost už NEMÁ vlastný Scaffold, používa padding z MainScreen
    NavHost(
        navController = nestedNavController,
        startDestination = Routes.TUTORIALS_LIST,
        modifier = Modifier.padding(paddingValues) // Padding sa aplikuje na všetky obrazovky vnútri
    ) {
        composable(Routes.TUTORIALS_LIST) {
            TutorialsScreen(
                navController = nestedNavController,
                viewModel = tutorialsViewModel
                // Modifier netreba, NavHost ho už aplikuje
            )
        }
        composable(
            route = Routes.TUTORIAL_DETAIL,
            arguments = listOf(navArgument("tutorialId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tutorialId = backStackEntry.arguments?.getString("tutorialId")
            TutorialDetailScreen(
                navController = nestedNavController,
                tutorialsViewModel = tutorialsViewModel,
                sharedViewModel = sharedViewModel,
                tutorialId = tutorialId,
                onNavigateToEdit = { id ->
                    nestedNavController.navigate(Routes.editTutorial(id))
                }
                // Modifier netreba, NavHost ho už aplikuje
            )
        }
        composable(Routes.ADD_TUTORIAL) {
            AddTutorialScreen(
                navController = nestedNavController,
                tutorialsViewModel = tutorialsViewModel,
                sharedViewModel = sharedViewModel
            )
        }
        composable(
            route = Routes.EDIT_TUTORIAL,
            arguments = listOf(navArgument("tutorialId") { type = NavType.StringType })
        ) { backStackEntry ->
            AddTutorialScreen(
                navController = nestedNavController,
                tutorialsViewModel = tutorialsViewModel,
                sharedViewModel = sharedViewModel
            )
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
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
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
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
