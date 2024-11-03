package com.example.appprueba

import MyNavigationBar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appprueba.ui.theme.AppPruebaTheme
import Login

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object ContinentSelector : Screen("continent_selector")
    object CountrySelector : Screen("country_selector/{country}") {
        fun createRoute(country: String) = "country_selector/$country"
    }
    object SearchEmployee : Screen("search_employee")
    object Settings : Screen("settings")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            var isLoggedIn by remember { mutableStateOf(false) }

            AppPruebaTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) Screen.SearchEmployee.route else Screen.Login.route
                    ) {
                        composable(Screen.Login.route) {
                            Login(onLoginSuccess = {
                                isLoggedIn = true
                                navController.navigate(Screen.SearchEmployee.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            })
                        }
                        composable(Screen.SearchEmployee.route) {
                            if (isLoggedIn) {
                                Scaffold(
                                    bottomBar = {
                                        MyNavigationBar(navController)
                                    }
                                ) { innerPadding ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(innerPadding)
                                    ) {
                                        SearchGeneral()
                                    }
                                }
                            } else {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }
                        composable(Screen.Settings.route) {
                            if (isLoggedIn) {
                                Settings(
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                                    onLogout = {
                                        isLoggedIn = false
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
