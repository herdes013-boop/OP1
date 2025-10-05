package com.example.op

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    // ================== NOVÁ TRASA PRE TEST OBRAZOVKU ==================
    const val TEST_SCREEN = "test_screen"
    // =================================================================

    // --- HESLÁ ---
    const val PASSWORDS_LIST = "passwords_list"
    const val ADD_PASSWORD = "add_password"
    const val PASSWORD_DETAIL = "password_detail/{passwordId}"
    const val EDIT_PASSWORD = "edit_password/{passwordId}"
    fun passwordDetail(passwordId: String) = "password_detail/$passwordId"
    fun editPassword(passwordId: String) = "edit_password/$passwordId"

    // --- IP ADRESY (NOVÉ) ---
    const val ADD_IP_ADDRESS = "add_ip_address"
    const val EDIT_IP_ADDRESS = "edit_ip_address/{ipId}"
    fun editIpAddress(ipId: String) = "edit_ip_address/$ipId"

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
        // Zabezpečíme správne zobrazenie spodnej lišty pre hlavné aj testovacie obrazovky
        val isTestRoute = currentRoute?.startsWith("test_") == true
        if (isTestRoute) {
            sharedViewModel.setShowBottomBar(false)
            return@LaunchedEffect
        }

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
                            icon = { Icon(screen.icon, "Ikona spodnej navigácie") },
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
                HomeScreen(
                    navController = navController,
                    modifier = Modifier.padding(paddingValues)
                )
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
            composable(Routes.TEST_SCREEN) {
                TestScreen(
                    navController = navController,
                    sharedViewModel = sharedViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            // =================== FINÁLNA ZMENA JE TU ===================
            // Pridali sme definíciu pre novú trasu "test_detail/{itemId}"
            composable(
                route = "test_detail/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId")
                if (itemId != null) {
                    // Zobrazíme novú obrazovku TestDetailScreen
                    TestDetailScreen(
                        navController = navController,
                        sharedViewModel = sharedViewModel,
                        itemId = itemId
                    )
                }
            }
            // =========================================================
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
    }

    NavHost(
        navController = nestedNavController,
        startDestination = Routes.PASSWORDS_LIST,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Routes.PASSWORDS_LIST) {
            PasswordsScreen(
                navController = nestedNavController,
                viewModel = viewModel,
                sharedViewModel = sharedViewModel
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
                    sharedViewModel = sharedViewModel,
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

        composable(Routes.ADD_IP_ADDRESS) {
            AddEditIpAddressScreen(
                navController = nestedNavController,
                viewModel = viewModel
            )
        }
        composable(
            route = Routes.EDIT_IP_ADDRESS,
            arguments = listOf(navArgument("ipId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ipId = backStackEntry.arguments?.getString("ipId")
            AddEditIpAddressScreen(
                navController = nestedNavController,
                viewModel = viewModel,
                ipId = ipId
            )
        }
    }
}

@Composable
fun ContactsNavHost(
    viewModel: ContactsViewModel,
    paddingValues: PaddingValues,
    sharedViewModel: SharedViewModel
) {
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        val currentRoute = navBackStackEntry?.destination?.route
        val isSubScreen = currentRoute != Routes.CONTACTS_LIST
        sharedViewModel.setShowBottomBar(!isSubScreen)

        val isAddScreen = currentRoute == Routes.ADD_CONTACT

        if (isAddScreen) {
            sharedViewModel.setTopBarState(TopBarState(isVisible = false))
        } else if (!isSubScreen) { // Táto podmienka je chybná, mala by byť viazaná na hlavný zoznam
            // Správne by to malo byť:
            // if (currentRoute == Routes.CONTACTS_LIST) {
            //     sharedViewModel.setTopBarState(TopBarState(title = "Kontakty", isVisible = true))
            // }
        }
    }

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
                sharedViewModel = sharedViewModel,
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
    paddingValues: PaddingValues
) {
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        val currentRoute = navBackStackEntry?.destination?.route
        val isAddOrEditScreen = currentRoute == Routes.ADD_TUTORIAL || currentRoute?.startsWith("edit_tutorial/") == true

        sharedViewModel.setShowBottomBar(currentRoute == Routes.TUTORIALS_LIST)

        if (isAddOrEditScreen) {
            sharedViewModel.setTopBarState(TopBarState(isVisible = false))
        } else if(currentRoute == Routes.TUTORIALS_LIST) {
            sharedViewModel.setTopBarState(TopBarState(title = "Návody", isVisible = true))
        }
    }

    NavHost(
        navController = nestedNavController,
        startDestination = Routes.TUTORIALS_LIST,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Routes.TUTORIALS_LIST) {
            TutorialsScreen(
                navController = nestedNavController,
                viewModel = tutorialsViewModel
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
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
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

        Spacer(Modifier.height(48.dp))
        Button(
            onClick = { navController.navigate(Routes.TEST_SCREEN) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Otvoriť Test Screen")
        }
    }
}
