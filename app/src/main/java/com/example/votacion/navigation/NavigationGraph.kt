package com.example.votacion.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.votacion.features.auth.presentation.screens.LoginScreen
import com.example.votacion.features.auth.presentation.screens.RegisterScreen
import com.example.votacion.features.polls.presentation.screens.PollsScreen
import com.example.votacion.features.polls.presentation.screens.CreatePollScreen
import com.example.votacion.features.polls.presentation.screens.EditPollScreen
import com.example.votacion.features.auth.presentation.viewmodel.AuthViewModel

@Composable
fun NavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val uiState = authViewModel.uiState.value
    val startDestination = if (uiState.isAuthenticated) {
        Screen.Polls.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Polls.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Polls.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Polls.route) {
            PollsScreen(
                onNavigateToCreatePoll = {
                    navController.navigate(Screen.CreatePoll.route)
                },                onNavigateToEditPoll = { pollId ->
                    navController.navigate(Screen.EditPoll.createRoute(pollId))
                },                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Polls.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreatePoll.route) {
            CreatePollScreen(
                onSuccess = {
                    navController.navigate(Screen.Polls.route) {
                        popUpTo(Screen.CreatePoll.route) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditPoll.route) { backStackEntry ->
            val pollId = backStackEntry.arguments?.getString("pollId") ?: ""
            EditPollScreen(
                onDone = {
                    navController.navigate(Screen.Polls.route) {
                        popUpTo(Screen.EditPoll.route) { inclusive = true }
                    }
                },
                onDelete = {
                    navController.navigate(Screen.Polls.route) {
                        popUpTo(Screen.EditPoll.route) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
