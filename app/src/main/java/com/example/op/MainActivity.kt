package com.example.op

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import com.example.op.ui.theme.TelekomMagenta


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
    const val TEST_SCREEN = "test_screen"

    const val PASSWORDS_LIST = "passwords_list"
    const val ADD_PASSWORD = "add_password"
    const val PASSWORD_DETAIL = "password_detail/{passwordId}"
    const val EDIT_PASSWORD = "edit_password/{passwordId}"
    fun passwordDetail(passwordId: String) = "password_detail/$passwordId"
    fun editPassword(passwordId: String) = "edit_password/$passwordId"

    const val ADD_IP_ADDRESS = "add_ip_address"
    const val EDIT_IP_ADDRESS = "edit_ip_address/{ipId}"
    fun editIpAddress(ipId: String) = "edit_ip_address/$ipId"

    const val CONTACTS_LIST = "contacts_list"
    const val CONTACT_DETAIL = "contact_detail/{contactId}"
    const val ADD_CONTACT = "add_contact"
    const val EDIT_CONTACT = "edit_contact/{contactId}"
    fun editContact(contactId: Int) = "edit_contact/$contactId"
    const val MANAGE_CHANNELS = "manage_channels"

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
        val isTestRoute = currentRoute?.startsWith("test_") == true
        if (isTestRoute) {
            sharedViewModel.setShowBottomBar(false)
            return@LaunchedEffect
        }

        when (currentRoute) {
            // ✅ ZMENA 1: ODDEĽTE HOME_ROOT OD OSTATNÝCH
            Routes.HOME_ROOT -> {
                sharedViewModel.setShowBottomBar(true)
                sharedViewModel.setTopBarState(
                    TopBarState(
                        title = "Domov",
                        isVisible = true,
                        // TOTO JE KĽÚČOVÉ: Pridanie ikony menu
                        navigationIcon = {
                            IconButton(onClick = { navController.navigate(Routes.SETTINGS_ROOT) }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                )
            }
            // Pôvodná logika pre heslá a kontakty zostáva
            Routes.PASSWORDS_ROOT, Routes.CONTACTS_ROOT -> {
                sharedViewModel.setShowBottomBar(true)
                val title = if (currentRoute == Routes.PASSWORDS_ROOT) "Heslá" else "Kontakty"
                // TU IKONA NIE JE, ČO JE SPRÁVNE
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
                    actions = topBarState.actions ?: {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TelekomMagenta,
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
                                // ✅ ZAČIATOK ZMENY
                                // Ak opúšťame sekciu hesiel, povieme to jej ViewModelu
                                val isLeavingPasswords = currentDestination?.hierarchy?.any { it.route == Routes.PASSWORDS_ROOT } == true &&
                                        screen.route != Routes.PASSWORDS_ROOT
                                if (isLeavingPasswords) {
                                    passwordsViewModel.onExitedMainRoute()
                                }
                                // ✅ KONIEC ZMENY

                                // Pôvodná navigačná logika
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
        NavHost(
            navController = navController,startDestination = Routes.HOME_ROOT,
            // ✅ ZAČIATOK ZMENY: Tieto 3 riadky vypnú animácie
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popExitTransition = { ExitTransition.None }
            // ✅ KONIEC ZMENY
        ) {
            composable(Routes.HOME_ROOT) {
                HomeScreen(
                    navController = navController,
                    modifier = Modifier.padding(paddingValues),
                    sharedViewModel = sharedViewModel
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
            composable(
                route = "test_detail/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId")
                if (itemId != null) {
                    TestDetailScreen(
                        navController = navController,
                        sharedViewModel = sharedViewModel,
                        itemId = itemId
                    )
                }
            }
        }
    }
}


@Composable
fun PasswordsNavHost(viewModel: PasswordsViewModel, paddingValues: PaddingValues, sharedViewModel: SharedViewModel) {
    val nestedNavController = rememberNavController()

    // Spodná lišta sa schováva, keď nie sme na hlavnom zozname
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        val currentRoute = navBackStackEntry?.destination?.route
        sharedViewModel.setShowBottomBar(currentRoute == Routes.PASSWORDS_LIST)
    }

    NavHost(
        navController = nestedNavController,
        startDestination = Routes.PASSWORDS_LIST,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(Routes.PASSWORDS_LIST) {
            // ✅ ZMENA: Odstránili sme "backStackEntry ->" a parameter navBackStackEntry
            PasswordsScreen(
                modifier = Modifier.padding(paddingValues),
                navController = nestedNavController,
                viewModel = viewModel,
                sharedViewModel = sharedViewModel
            )
        }

        // ✅ ZMENA: Nová spoločná cesta pre detail hesla aj IP
        composable(
            route = "item_detail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            ItemDetailScreen(
                modifier = Modifier.padding(paddingValues),
                itemId = itemId,
                viewModel = viewModel,
                sharedViewModel = sharedViewModel,
                onNavigateToEdit = { id ->
                    // Rozhodneme sa, kam navigovať na základe toho, či ide o heslo alebo IP
                    val isPassword = viewModel.passwordList.value.any { it.id == id }
                    if (isPassword) {
                        nestedNavController.navigate(Routes.editPassword(id))
                    } else {
                        nestedNavController.navigate(Routes.editIpAddress(id))
                    }
                },
                onBack = { nestedNavController.popBackStack() }
            )
        }

        // Obrazovky pre pridanie/úpravu zostávajú rovnaké
        composable(Routes.ADD_PASSWORD) {
            AddEditPasswordScreen(
                modifier = Modifier.padding(paddingValues),
                navController = nestedNavController,
                viewModel = viewModel,
                sharedViewModel = sharedViewModel
            )
        }
        composable(
            route = Routes.EDIT_PASSWORD,
            arguments = listOf(navArgument("passwordId") { type = NavType.StringType })
        ) { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getString("passwordId")
            AddEditPasswordScreen(
                modifier = Modifier.padding(paddingValues),
                navController = nestedNavController,
                viewModel = viewModel,
                passwordId = passwordId,
                sharedViewModel = sharedViewModel
            )
        }
        composable(Routes.ADD_IP_ADDRESS) {
            AddEditIpAddressScreen(
                modifier = Modifier.padding(paddingValues),
                navController = nestedNavController,
                viewModel = viewModel,
                sharedViewModel = sharedViewModel
            )
        }
        composable(
            route = Routes.EDIT_IP_ADDRESS,
            arguments = listOf(navArgument("ipId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ipId = backStackEntry.arguments?.getString("ipId")
            AddEditIpAddressScreen(
                modifier = Modifier.padding(paddingValues),
                navController = nestedNavController,
                viewModel = viewModel,
                ipId = ipId,
                sharedViewModel = sharedViewModel
            )
        }
    }
}

// ✅✅✅ JEDINÁ ZMENA JE V TOMTO BLOKU ✅✅✅
@Composable
fun ContactsNavHost(
    viewModel: ContactsViewModel,
    paddingValues: PaddingValues,
    sharedViewModel: SharedViewModel,
) {
    val nestedNavController = rememberNavController()

    // Logika pre zobrazenie/skrytie spodnej lišty
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        val currentRoute = navBackStackEntry?.destination?.route
        sharedViewModel.setShowBottomBar(currentRoute == Routes.CONTACTS_LIST)
    }

    NavHost(
        navController = nestedNavController,
        startDestination = Routes.CONTACTS_LIST,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        // 1. Obrazovka so zoznamom kontaktov
        composable(Routes.CONTACTS_LIST) {
            sharedViewModel.setTopBarState(TopBarState(title = "Kontakty", isVisible = true))
            ContactsScreen(
                modifier = Modifier.padding(paddingValues),
                navController = nestedNavController,
                viewModel = viewModel
            )
        }

        // ✅✅✅ 2. NOVÁ OBRAZOVKA PRE DETAIL KONTAKTU ✅✅✅
        // Skontrolujte, či máte tento blok a či používa Routes.CONTACT_DETAIL
        composable(
            route = Routes.CONTACT_DETAIL,
            arguments = listOf(navArgument("contactId") { type = NavType.IntType })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getInt("contactId") ?: 0
            ContactDetailScreen(
                modifier = Modifier.padding(paddingValues),
                contactId = contactId,
                viewModel = viewModel,
                sharedViewModel = sharedViewModel,
                onNavigateToEdit = { id ->
                    nestedNavController.navigate(Routes.editContact(id))
                },
                onBack = {
                    viewModel.clearSelectedContact() // Dôležité pre vyčistenie stavu
                    nestedNavController.popBackStack()
                }
            )
        }

        // 3. Obrazovka pre úpravu kontaktu
        composable(
            route = Routes.EDIT_CONTACT,
            arguments = listOf(navArgument("contactId") { type = NavType.IntType })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getInt("contactId") ?: 0
            EditContactScreen(
                modifier = Modifier.padding(paddingValues),
                navController = nestedNavController,
                contactId = contactId,
                viewModel = viewModel,
                sharedViewModel = sharedViewModel,
                onBack = { nestedNavController.popBackStack() }
            )
        }

        // 4. Ostatné obrazovky (pridanie, správa kanálov)
        composable(Routes.ADD_CONTACT) {
            sharedViewModel.setTopBarState(TopBarState(isVisible = false))
            AddContactScreen(
                navController = nestedNavController,
                viewModel = viewModel
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
    paddingValues: PaddingValues,
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
        // ✅ ZAČIATOK ZMENY
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popExitTransition = { ExitTransition.None }
        // ✅ KONIEC ZMENY
    ) {
        composable(Routes.TUTORIALS_LIST) {
            TutorialsScreen(
                modifier = Modifier.padding(paddingValues),
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
                modifier = Modifier.padding(paddingValues),
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
                modifier = Modifier.padding(paddingValues),
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
                modifier = Modifier.padding(paddingValues),
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
fun HomeScreen(navController: NavController, sharedViewModel: SharedViewModel, modifier: Modifier = Modifier) {

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

