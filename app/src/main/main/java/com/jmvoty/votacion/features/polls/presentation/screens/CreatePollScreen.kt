@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.jmvoty.votacion.features.polls.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jmvoty.votacion.features.polls.presentation.viewmodel.CreatePollViewModel

@Composable
fun CreatePollScreen(
    viewModel: CreatePollViewModel = hiltViewModel(),
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updateOptionImage(uri)
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Encuesta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Atrás")
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
                    style = MaterialTheme.typography.titleSmall
                )
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    placeholder = { Text("Ej: ¿Cuál es tu comida favorita?") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    maxLines = 2
                )
            }

            item {
                Text(
                    "Opciones de voto (mínimo 2)",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            itemsIndexed(uiState.options) { index, option ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = option.text,
                            onValueChange = { viewModel.updateOptionText(index, it) },
                            label = { Text("Opción ${index + 1}") },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isLoading
                        )

                        IconButton(onClick = {
                            viewModel.pendingImageIndex = index // Guardar qué opción estamos editando
                            launcher.launch("image/*")
                        }) {
                            Icon(
                                imageVector = if (option.imageUri != null) Icons.Default.CheckCircle else Icons.Default.Image,
                                contentDescription = "Imagen",
                                tint = if (option.imageUri != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }

                    // Reemplaza tu AsyncImage por una real con Coil (Añade la librería Coil al build.gradle)
                    if (option.imageUri != null) {
                        AsyncImage(
                            model = option.imageUri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(top = 8.dp)
                                .clip(MaterialTheme.shapes.small),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }


            item {
                TextButton(
                    onClick = { viewModel.addOption() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
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
                    onClick = { viewModel.createPoll() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !uiState.isLoading && uiState.title.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Crear encuesta")
                    }
                }
            }
        }
    }
}

@Composable
fun AsyncImage(model: Uri, contentDescription: Nothing?, modifier: Modifier) {
    TODO("Not yet implemented")
}

