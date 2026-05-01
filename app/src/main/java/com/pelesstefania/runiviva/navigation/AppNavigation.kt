package com.pelesstefania.runiviva.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pelesstefania.runiviva.ui.screens.auth.ForgotPasswordScreen
import com.pelesstefania.runiviva.ui.screens.auth.LoginScreen
import com.pelesstefania.runiviva.ui.screens.auth.RegisterScreen
import com.pelesstefania.runiviva.ui.screens.HomeScreen
@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier
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
            HomeScreen()
        }
    }
}