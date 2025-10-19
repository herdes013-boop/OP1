package com.example.op

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.op.ui.theme.AppNavigationBar
import com.example.op.ui.theme.OPTheme
import com.example.op.ui.theme.TelekomMagenta
import kotlinx.coroutines.launch


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
    const val ABOUT_SCREEN = "about_screen" // Pridali sme cestu pre "O aplikácii"

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
    Screen.Contacts,
    Screen.Passwords,
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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentRoute) {
        val isTestRoute = currentRoute?.startsWith("test_") == true
        if (isTestRoute) {
            sharedViewModel.setShowBottomBar(false)
            return@LaunchedEffect
        }

        // Reset ikony, aby sa nezobrazovala na iných obrazovkách
        var currentNavIcon: (@Composable () -> Unit)? = null

        when {
            // Ak sme na niektorej z hlavných "root" obrazoviek, spodná lišta je viditeľná
            currentRoute in bottomNavItems.map { it.route } -> {
                sharedViewModel.setShowBottomBar(true)
            }
            // Ak sme inde (napr. detail, nastavenia), spodnú lištu skryjeme
            else -> {
                sharedViewModel.setShowBottomBar(false)
            }
        }

        when (currentRoute) {
            Routes.HOME_ROOT -> {
                // Logika pre Domov zostáva
                currentNavIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
                sharedViewModel.setTopBarState(
                    TopBarState(title = "Domov", isVisible = true, navigationIcon = currentNavIcon)
                )
            }
            // Ostatné prípady už nie sú potrebné, pretože top bar si riadia obrazovky samé
            // a o bottom bar sme sa postarali vyššie.
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute == Routes.HOME_ROOT,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Menu aplikácie",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // ✅ KROK 1: PRIDANÁ NOVÁ POLOŽKA "DOCHÁDZKA"
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Dochádzka") },
                    selected = false,
                    onClick = {
                        // Zatiaľ nerobí nič, len zatvorí menu
                        scope.launch { drawerState.close() }
                        // TODO: V budúcnosti navigovať na obrazovku dochádzky
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null) }, // Iba príklad ikony
                    label = { Text("Poznámky") }, // Váš názov
                    selected = false,
                    onClick = {
                        // Zatiaľ nerobí nič, len zatvorí menu
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Nastavenia") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.SETTINGS_ROOT)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("O aplikácii") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.ABOUT_SCREEN)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
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
                    // ✅ KROK 2: ZMEŇ "NavigationBar" NA "AppNavigationBar"
                    AppNavigationBar { // <-- TENTO NÁZOV SA ZMENIL
                        val currentDestination = navBackStackEntry?.destination
                        bottomNavItems.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, "Ikona spodnej navigácie") },
                                label = { Text(screen.label) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    // ✅ KROK 1: Zistíme, či opúšťame sekciu "Heslá" a ideme inam.
                                    // Podmienky:
                                    // 1. Práve sa nachádzame v sekcii Heslá (PASSWORDS_ROOT).
                                    val isCurrentlyInPasswords = currentDestination?.hierarchy?.any { it.route == Routes.PASSWORDS_ROOT } == true
                                    // 2. Cieľová cesta (screen.route) NIE JE sekcia Heslá.
                                    val isTargetOutsidePasswords = screen.route != Routes.PASSWORDS_ROOT

                                    // Ak platia obe podmienky, resetujeme ViewModel.
                                    if (currentDestination?.hierarchy?.any { it.route == Routes.PASSWORDS_ROOT } == true && screen.route != Routes.PASSWORDS_ROOT) {
                                        passwordsViewModel.resetTabToDefault()
                                    }

                                    // 2. Reset pre sekciu KONTAKTY
                                    if (currentDestination?.hierarchy?.any { it.route == Routes.CONTACTS_ROOT } == true && screen.route != Routes.CONTACTS_ROOT) {
                                        contactsViewModel.resetTabToDefault() // Toto teraz bude fungovať
                                    }

                                    // 3. Reset pre sekciu NÁVODY
                                    if (currentDestination?.hierarchy?.any { it.route == Routes.TUTORIALS_ROOT } == true && screen.route != Routes.TUTORIALS_ROOT) {
                                        tutorialsViewModel.resetTabToDefault() // Aj toto bude fungovať
                                    }

                                    // ✅ KROK 2: Vždy vykonáme navigáciu na zvolenú obrazovku.
                                    // Táto časť bola v mojom predchádzajúcom návrhu omylom vynechaná.
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
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Routes.HOME_ROOT,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                composable(Routes.HOME_ROOT) {
                    HomeScreen(
                        navController = navController,
                        sharedViewModel = sharedViewModel,
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
                    SettingsScreen(
                        navController = navController,
                        sharedViewModel = sharedViewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                composable(Routes.ABOUT_SCREEN) {
                    AboutScreen(
                        navController = navController,
                        sharedViewModel = sharedViewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
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
}

// =========================================================================
// Ostatné NavHosty (PasswordsNavHost, ContactsNavHost, atď.) idú sem.
// Keďže sa nemenili, pre prehľadnosť ich tu nevypisujem znova.
// Vložte sem váš existujúci kód pre PasswordsNavHost, ContactsNavHost, TutorialsNavHost.
// =========================================================================


@Composable
fun PasswordsNavHost(viewModel: PasswordsViewModel, paddingValues: PaddingValues, sharedViewModel: SharedViewModel) {
    val nestedNavController = rememberNavController()

    NavHost(
        navController = nestedNavController,
        startDestination = Routes.PASSWORDS_LIST,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(Routes.PASSWORDS_LIST) {
            PasswordsScreen(
                modifier = Modifier.padding(paddingValues),
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
                    modifier = Modifier.padding(paddingValues),
                    passwordId = passwordId,
                    viewModel = viewModel,
                    sharedViewModel = sharedViewModel,
                    onNavigateToEdit = { id ->
                        nestedNavController.navigate(Routes.editPassword(id))
                    },
                    onBack = { nestedNavController.popBackStack() }
                )
            }
        }
        composable(
            route = "ip_detail/{ipId}",
            arguments = listOf(navArgument("ipId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ipId = backStackEntry.arguments?.getString("ipId")
            if (ipId != null) {
                IpDetailScreen(
                    modifier = Modifier.padding(paddingValues),
                    ipId = ipId,
                    viewModel = viewModel,
                    sharedViewModel = sharedViewModel,
                    onNavigateToEdit = { id ->
                        nestedNavController.navigate(Routes.editIpAddress(id))
                    },
                    onBack = { nestedNavController.popBackStack() }
                )
            }
        }

        // --- HESLÁ (UŽ HOTOVÉ) ---
        composable(Routes.ADD_PASSWORD) {
            AddPasswordScreen(
                modifier = Modifier.padding(paddingValues),
                navController = nestedNavController,
                viewModel = viewModel,
                sharedViewModel = sharedViewModel,
                onBack = { nestedNavController.popBackStack() }
            )
        }
        composable(
            route = Routes.EDIT_PASSWORD,
            arguments = listOf(navArgument("passwordId") { type = NavType.StringType })
        ) { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getString("passwordId")
            if (passwordId != null) {
                EditPasswordScreen(
                    modifier = Modifier.padding(paddingValues),
                    navController = nestedNavController,
                    viewModel = viewModel,
                    passwordId = passwordId,
                    sharedViewModel = sharedViewModel,
                    onBack = {
                        nestedNavController.popBackStack()
                        nestedNavController.popBackStack()
                    }
                )
            }
        }

        // --- ZAČIATOK ZMIEN PRE IP ADRESY ---

        // 1. NOVÁ OBRAZOVKA PRE PRIDANIE IP ADRESY
        composable(Routes.ADD_IP_ADDRESS) {
            AddIpAddressScreen(
                modifier = Modifier.padding(paddingValues),
                navController = nestedNavController,
                viewModel = viewModel,
                sharedViewModel = sharedViewModel,
                onBack = { nestedNavController.popBackStack() } // Jednoduchý návrat
            )
        }

        // 2. NOVÁ OBRAZOVKA PRE EDITÁCIU IP ADRESY
        composable(
            route = Routes.EDIT_IP_ADDRESS,
            arguments = listOf(navArgument("ipId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ipId = backStackEntry.arguments?.getString("ipId")
            if (ipId != null) {
                EditIpAddressScreen(
                    modifier = Modifier.padding(paddingValues),
                    navController = nestedNavController,
                    viewModel = viewModel,
                    ipId = ipId,
                    sharedViewModel = sharedViewModel,
                    onBack = {
                        // Návrat o dva kroky: z Editácie cez Detail na Zoznam
                        nestedNavController.popBackStack()
                        nestedNavController.popBackStack()
                    }
                )
            }
        }

        // --- KONIEC ZMIEN PRE IP ADRESY ---
    }
}

@Composable
fun ContactsNavHost(
    viewModel: ContactsViewModel,
    paddingValues: PaddingValues,
    sharedViewModel: SharedViewModel,
) {
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        val currentRoute = navBackStackEntry?.destination?.route
        sharedViewModel.setShowBottomBar(currentRoute == Routes.CONTACTS_LIST)
        if (currentRoute == Routes.CONTACTS_LIST) {
            sharedViewModel.setTopBarState(TopBarState(title = "Kontakty"))
        }
    }

    NavHost(
        navController = nestedNavController,
        startDestination = Routes.CONTACTS_LIST,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(Routes.CONTACTS_LIST) {
            ContactsScreen(
                navController = nestedNavController,
                sharedViewModel = sharedViewModel,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }

        // ✅ PRIDANÝ CHÝBAJÚCI BLOK PRE PRIDANIE KONTAKTU
        composable(Routes.ADD_CONTACT) {
            AddContactScreen(
                modifier = Modifier.padding(paddingValues),
                navController = nestedNavController,
                viewModel = viewModel,
                sharedViewModel = sharedViewModel,
                onBack = { nestedNavController.popBackStack() } // Návrat o 1 krok
            )
        }

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
                    nestedNavController.popBackStack()
                }
            )
        }

        // ✅ JEDEN, SPRÁVNY BLOK PRE ÚPRAVU KONTAKTU
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
                // SPRÁVNY NÁVRAT O 2 KROKY (z Editácie cez Detail na Zoznam)
                onBack = {
                    nestedNavController.popBackStack()
                    nestedNavController.popBackStack()
                }
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
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popExitTransition = { ExitTransition.None }
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

// NOVÉ: Composable pre editáciu návodu
        composable(
            route = Routes.EDIT_TUTORIAL,
            arguments = listOf(navArgument("tutorialId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Povinný argument tutorialId
            val tutorialId = backStackEntry.arguments?.getString("tutorialId")
            if (tutorialId != null) {
                EditTutorialScreen(
                    modifier = Modifier.padding(paddingValues),
                    navController = nestedNavController,
                    tutorialsViewModel = tutorialsViewModel,
                    sharedViewModel = sharedViewModel,
                    tutorialId = tutorialId
                )
            }
        }
    }
}


// =========================================================================
// Obrazovky (Screens)
// =========================================================================

@Composable
fun SettingsScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        sharedViewModel.setTopBarState(
            TopBarState(
                title = "Nastavenia",
                isVisible = true,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Naspäť"
                        )
                    }
                }
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        item {
            SettingsItem(
                title = "Spravovať kategórie",
                subtitle = "Pridajte alebo upravte kategórie návodov",
                icon = Icons.Default.Category,
                onClick = { /* TODO: Navigácia na správu kategórií */ }
            )
        }
        item {
            SettingsItem(
                title = "Vzhľad",
                subtitle = "Nastavenie svetlého a tmavého režimu",
                icon = Icons.Default.Style,
                onClick = { /* TODO: Navigácia na nastavenia vzhľadu */ }
            )
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun AboutScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        sharedViewModel.setTopBarState(
            TopBarState(
                title = "O aplikácii",
                isVisible = true,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Naspäť"
                        )
                    }
                }
            )
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Info, "Ikona info", Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("OP Správca", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Verzia 1.0.0", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Text("Aplikácia pre bezpečnú správu vašich dát.", textAlign = TextAlign.Center)
    }
}


@Composable
fun HomeScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
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

// Sem patrí vaša obrazovka TestScreen, ak ju máte definovanú inde,
// inak ju môžete nechať tu.


