package com.jmvoty.votacion.features.polls.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jmvoty.votacion.features.polls.domain.entities.Poll
import com.jmvoty.votacion.features.polls.domain.entities.PollOption


@Composable
fun PollCard(
    poll: Poll,
    onVote: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    poll.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                poll.options.forEach { option ->
                    PollOptionItem(
                        option = option,
                        isVoted = poll.voted && poll.selectedOptionId == option.id,
                        totalVotes = poll.totalVotes,
                        onVote = { onVote(option.id) },
                        isEnabled = !poll.voted && poll.isOpen
                    )
                }
            }

            // Footer de la Card
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (poll.voted) {
                    Text("✓ Votado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                if (!poll.isOpen) {
                    Text("Cerrada", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
                Text("${poll.totalVotes} votos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun PollOptionItem(
    option: PollOption,
    isVoted: Boolean,
    totalVotes: Int,
    onVote: () -> Unit,
    isEnabled: Boolean
) {
    androidx.compose.runtime.LaunchedEffect(option.imageUrl) {
        android.util.Log.d("POLL_DEBUG", "Opción: ${option.text} | URL recibida: ${option.imageUrl}")
    }
    val percentage = if (totalVotes > 0) (option.votesCount * 100) / totalVotes else 0

    OutlinedButton(
        onClick = onVote,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (option.imageUrl != null) 80.dp else 48.dp), // Más alto si hay imagen
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isVoted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Muestra la imagen si existe
            if (!option.imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = option.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onState = { state ->
                            when (state) {
                                is coil.compose.AsyncImagePainter.State.Error -> {
                                    android.util.Log.e("COIL_ERROR", "Fallo en: ${option.imageUrl}", state.result.throwable)
                                }
                                is coil.compose.AsyncImagePainter.State.Success -> {
                                    android.util.Log.i("COIL_SUCCESS", "Cargada: ${option.imageUrl}")
                                }
                                else -> {}
                            }
                        }
                    )

                    // Opcional: Puedes añadir un indicador visual de carga/error aquí
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    option.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!isEnabled || isVoted) {
                    LinearProgressIndicator(
                        progress = { percentage / 100f },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }

            Text(
                "$percentage%",
                style = MaterialTheme.typography.labelLarge,
                color = if (isVoted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}