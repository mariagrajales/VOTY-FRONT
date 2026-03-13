@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.votacion.features.polls.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.votacion.features.polls.data.models.PollOutput
import com.example.votacion.features.auth.presentation.viewmodel.AuthViewModel
import com.example.votacion.features.polls.domain.entities.Poll
import com.example.votacion.features.polls.presentation.components.PollCard
import com.example.votacion.features.polls.presentation.viewmodel.PollsViewModel


@Composable
fun PollsScreen(
    pollsViewModel: PollsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToCreatePoll: () -> Unit,
    onNavigateToEditPoll: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by pollsViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        pollsViewModel.loadPolls()
    }

    DisposableEffect(Unit) {
        pollsViewModel.shakeDetector.startListening()
        onDispose {
            pollsViewModel.shakeDetector.stopListening()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Encuestas") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) { Icon(Icons.Default.Person, "Perfil") }
                    IconButton(onClick = { authViewModel.logout(); onLogout() }) { Icon(Icons.Default.Logout, "Salir") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreatePoll) { Icon(Icons.Default.Add, "Crear") }
        }
    ) { padding ->
        // Manejo de Estados de UI
        when {
            uiState.isLoading -> LoadingBox(padding)
            uiState.error != null -> ErrorBox(uiState.error!!, padding) { pollsViewModel.loadPolls() }
            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    PollList(uiState.polls, padding, pollsViewModel, onNavigateToEditPoll)
                    if (uiState.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(padding)
                                .padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PollList(
    polls: List<Poll>,
    padding: PaddingValues,
    viewModel: PollsViewModel,
    onEdit: (String) -> Unit
) {
    if (polls.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay encuestas disponibles", color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(polls, key = { it.id }) { poll ->
                PollCard(
                    poll = poll,
                    onVote = { optionId -> viewModel.castVote(poll.id, optionId) },
                    onEdit = { onEdit(poll.id) },
                    onDelete = { viewModel.deletePoll(poll.id) }
                )
            }
        }
    }
}

@Composable
fun LoadingBox(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorBox(message: String, padding: PaddingValues, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}