package com.pelesstefania.runiviva.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.pelesstefania.runiviva.ui.screens.CalendarScreen
import com.pelesstefania.runiviva.ui.screens.FriendsScreen
import com.pelesstefania.runiviva.ui.screens.HomeScreen
import com.pelesstefania.runiviva.ui.screens.ProfileScreen
import com.pelesstefania.runiviva.ui.screens.RunScreen
import com.pelesstefania.runiviva.ui.screens.auth.ForgotPasswordScreen
import com.pelesstefania.runiviva.ui.screens.auth.LoginScreen
import com.pelesstefania.runiviva.ui.screens.auth.RegisterScreen
import com.pelesstefania.runiviva.ui.screens.RunDayDetailsScreen

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem(
            route = Routes.HOME,
            label = "Home",
            icon = Icons.Default.Home
        ),
        BottomNavItem(
            route = Routes.RUN,
            label = "Run",
            icon = Icons.AutoMirrored.Filled.DirectionsRun
        ),
        BottomNavItem(
            route = Routes.CALENDAR,
            label = "Calendar",
            icon = Icons.Default.DateRange
        ),
        BottomNavItem(
            route = Routes.FRIENDS,
            label = "Friends",
            icon = Icons.Default.Group
        ),
        BottomNavItem(
            route = Routes.PROFILE,
            label = "Profile",
            icon = Icons.Default.Person
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBottomBar =
        currentRoute == Routes.HOME ||
                currentRoute == Routes.RUN ||
                currentRoute == Routes.CALENDAR ||
                currentRoute == Routes.FRIENDS ||
                currentRoute == Routes.PROFILE

    val backgroundColor = Color(0xFFD9F0FF)
    val dividerColor = Color(0xFF4B67A1)

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(
                        color = dividerColor,
                        thickness = 1.dp
                    )

                    NavigationBar(
                        containerColor = backgroundColor,
                        tonalElevation = 0.dp
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected =
                                currentDestination?.hierarchy?.any {
                                    it.route == item.route
                                } == true

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(
                                            navController.graph.findStartDestination().id
                                        ) {
                                            saveState = true
                                        }

                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = null,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = dividerColor,
                                    unselectedIconColor = dividerColor,
                                    indicatorColor = Color(0xFFBFD7F2)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->

        val startDestination =
            if (FirebaseAuth.getInstance().currentUser != null) {
                Routes.HOME
            } else {
                Routes.LOGIN
            }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(navController)
            }

            composable(Routes.REGISTER) {
                RegisterScreen(navController)
            }

            composable(Routes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(navController)
            }

            composable(Routes.HOME) {
                HomeScreen(navController)
            }

            composable(Routes.RUN) {
                RunScreen(navController)
            }

            composable(Routes.CALENDAR) {
                CalendarScreen(navController)
            }

            composable(Routes.FRIENDS) {
                FriendsScreen()
            }

            composable(Routes.PROFILE) {
                ProfileScreen(navController)
            }

            composable("${Routes.RUN_DAY_DETAILS}/{date}") { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date") ?: ""
                RunDayDetailsScreen(
                    navController = navController,
                    date = date
                )
            }
        }
    }
}