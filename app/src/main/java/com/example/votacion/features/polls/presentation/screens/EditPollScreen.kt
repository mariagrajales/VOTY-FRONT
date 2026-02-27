@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.votacion.features.polls.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.votacion.features.polls.presentation.viewmodel.EditPollViewModel

@Composable
fun EditPollScreen(
    viewModel: EditPollViewModel = hiltViewModel(),
    onDone: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onDone()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Encuesta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.deletePoll() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar encuesta")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Título de la encuesta",
                    style = MaterialTheme.typography.labelMedium
                )
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Ej: ¿Cuál es tu comida favorita?") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    maxLines = 2
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.isOpen,
                        onCheckedChange = { viewModel.toggleOpen() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Encuesta abierta")
                }
            }

            item {
                Text(
                    "Opciones de voto (mínimo 2)",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            itemsIndexed(uiState.options) { index, option ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = option,
                        onValueChange = { viewModel.updateOption(index, it) },
                        label = { Text("Opción ${index + 1}") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        enabled = !uiState.isLoading,
                        maxLines = 1
                    )

                    if (uiState.options.size > 2) {
                        IconButton(
                            onClick = { viewModel.removeOption(index) },
                            modifier = Modifier.size(40.dp),
                            enabled = !uiState.isLoading
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Eliminar opción")
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { viewModel.addOption() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir opción")
                }
            }

            if (!uiState.error.isNullOrEmpty()) {
                item {
                    Text(
                        uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(12.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.saveChanges() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !uiState.isLoading && uiState.title.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Guardar cambios")
                    }
                }
            }
        }
    }
}
