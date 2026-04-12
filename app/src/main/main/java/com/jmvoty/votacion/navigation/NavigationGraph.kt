package com.jmvoty.votacion.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jmvoty.votacion.features.auth.presentation.screens.LoginScreen
import com.jmvoty.votacion.features.auth.presentation.screens.RegisterScreen
import com.jmvoty.votacion.features.polls.presentation.screens.PollsScreen
import com.jmvoty.votacion.features.polls.presentation.screens.CreatePollScreen
import com.jmvoty.votacion.features.polls.presentation.screens.EditPollScreen
import com.jmvoty.votacion.features.profile.presentation.screens.ProfileScreen
import com.jmvoty.votacion.features.auth.presentation.viewmodel.AuthViewModel

@Composable
fun NavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Si aún está verificando el token, mostramos una carga para evitar decisiones erróneas
    if (uiState.isCheckingAuth) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    
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
                        popUpTo(Screen.Login.route) { inclusive = true } }
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
                },
                onNavigateToEditPoll = { pollId ->
                    navController.navigate(Screen.EditPoll.createRoute(pollId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onLogout = {
                    authViewModel.logout() // IMPORTANTE: Limpiar sesión
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
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
