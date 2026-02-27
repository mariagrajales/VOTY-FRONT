package com.example.votacion.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Polls : Screen("polls")
    data object CreatePoll : Screen("create_poll")
    data object EditPoll : Screen("edit_poll/{pollId}") {
        fun createRoute(pollId: String) = "edit_poll/$pollId"
    }
}
