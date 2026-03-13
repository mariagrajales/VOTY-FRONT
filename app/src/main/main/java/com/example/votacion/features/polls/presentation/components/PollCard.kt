package com.example.votacion.features.polls.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.votacion.features.polls.domain.entities.Poll
import com.example.votacion.features.polls.domain.entities.PollOption

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
    val percentage = if (totalVotes > 0) (option.votesCount * 100) / totalVotes else 0

    OutlinedButton(
        onClick = onVote,
        enabled = isEnabled,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isVoted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(option.text, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$percentage%", style = MaterialTheme.typography.labelMedium)
        }
    }
}