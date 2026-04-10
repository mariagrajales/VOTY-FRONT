package com.jmvoty.votacion.features.polls.data.mapper

import com.jmvoty.votacion.features.polls.data.models.PollOutput
import com.jmvoty.votacion.features.polls.domain.entities.Poll
import com.jmvoty.votacion.features.polls.domain.entities.PollOption

fun PollOutput.toDomain(): Poll {
    return Poll(
        id = this.id,
        title = this.title,
        isOpen = this.isOpen,
        voted = this.voted,
        selectedOptionId = this.selectedOptionId,
        // Calculamos el total de votos sumando los de cada opción
        totalVotes = this.options.sumOf { it.votesCount ?: 0 },
        options = this.options.map { optionDto ->
            PollOption(
                id = optionDto.id,
                text = optionDto.text,
                votesCount = optionDto.votesCount ?: 0,
                // NUEVO: Mapeo de la URL de la imagen
                imageUrl = optionDto.imageUrl
            )
        }
    )
}