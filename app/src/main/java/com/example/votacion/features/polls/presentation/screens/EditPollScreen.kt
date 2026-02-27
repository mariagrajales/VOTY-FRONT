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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.votacion.features.polls.presentation.viewmodel.EditPollViewModel

@Composable
fun EditPollScreen(
    viewModel: EditPollViewModel = hiltViewModel(),
    onDone: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        if (uiState.isLoading && uiState.poll == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)

            ) {
                item {
                    if (!uiState.canEditOptions && !uiState.isLoading) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "⚠️ Edición Restringida",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    "Esta encuesta ya tiene votos registrados. Para proteger la integridad de los resultados, no es posible modificar la informacion de la encuesta.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
                item {
                    Text(
                        "Título de la encuesta",
                        style = MaterialTheme.typography.titleSmall
                    )
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        placeholder = { Text("Ej: ¿Cuál es tu comida favorita?") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading && uiState.canEditOptions,
                        maxLines = 2
                    )
                }

                item {
                    Surface(
                        onClick = { viewModel.toggleOpen() },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        enabled = !uiState.isLoading && uiState.canEditOptions
                        ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = uiState.isOpen,
                                onCheckedChange = { viewModel.toggleOpen() },
                                enabled = !uiState.isLoading && uiState.canEditOptions
                                )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (uiState.isOpen) "La encuesta está abierta para votar" 
                                else "La encuesta está cerrada",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                item {
                    Text(
                        "Opciones de voto (mínimo 2)",
                        style = MaterialTheme.typography.titleSmall
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
                                .height(64.dp),
                            enabled = !uiState.isLoading && uiState.canEditOptions,
                            maxLines = 1
                        )

                        if (uiState.options.size > 2) {
                            IconButton(
                                onClick = { viewModel.removeOption(index) },
                                enabled = !uiState.isLoading && uiState.canEditOptions
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Eliminar opción",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                item {
                    TextButton(
                        onClick = { viewModel.addOption() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading && uiState.canEditOptions
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir opción")
                    }
                }

                if (!uiState.error.isNullOrEmpty()) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                uiState.error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.saveChanges() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !uiState.isLoading && uiState.title.isNotBlank() &&
                                (uiState.canEditOptions || uiState.poll?.isOpen != uiState.isOpen)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Guardar cambios")
                        }
                    }
                }
            }
        }
    }
}
