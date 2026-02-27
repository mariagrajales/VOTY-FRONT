@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.votacion.features.polls.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.votacion.features.polls.data.models.PollOutput
import com.example.votacion.features.auth.presentation.viewmodel.AuthViewModel
import com.example.votacion.features.polls.presentation.viewmodel.PollsViewModel

@Composable
fun PollsScreen(
    pollsViewModel: PollsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToCreatePoll: () -> Unit,
    onNavigateToEditPoll: (String) -> Unit,
    onLogout: () -> Unit
) {
    val uiState = pollsViewModel.uiState.value

    // refresh when screen enters
    LaunchedEffect(Unit) {
        pollsViewModel.loadPolls()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Encuestas") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreatePoll) {
                Icon(Icons.Default.Add, contentDescription = "Crear encuesta")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!uiState.error.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = { pollsViewModel.refreshPolls() }) {
                        Text("Reintentar")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.polls) { poll ->
                    PollCard(
                        poll = poll,
                        onVote = { optionId ->
                            pollsViewModel.castVote(poll.id, optionId)
                        },
                        onEdit = { onNavigateToEditPoll(poll.id) },
                        onDelete = { pollsViewModel.deletePoll(poll.id) }
                    )
                }

                if (uiState.polls.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay encuestas disponibles")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PollCard(
    poll: PollOutput,
    onVote: (String) -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // optional action row
            if (onEdit != null || onDelete != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }
            Text(
                poll.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                poll.options.forEach { option ->
                    PollOptionItem(
                        option = option,
                        isVoted = poll.voted && poll.selectedOptionId == option.id,
                        totalVotes = poll.options.sumOf { it.votesCount },
                        onVote = { onVote(option.id) },
                        isEnabled = !poll.voted && poll.isOpen
                    )
                }
            }

            if (poll.voted) {
                Text(
                    "Ya has votado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            if (!poll.isOpen) {
                Text(
                    "Encuesta cerrada",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
fun PollOptionItem(
    option: com.example.votacion.features.polls.data.models.OptionOutput,
    isVoted: Boolean,
    totalVotes: Int,
    onVote: () -> Unit,
    isEnabled: Boolean
) {
    val percentage = if (totalVotes > 0) {
        (option.votesCount * 100) / totalVotes
    } else {
        0
    }

    Button(
        onClick = onVote,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isVoted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                option.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "${option.votesCount} votos",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    "$percentage%",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}