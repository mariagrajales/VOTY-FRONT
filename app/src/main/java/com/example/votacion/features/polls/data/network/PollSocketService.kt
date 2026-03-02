package com.example.votacion.features.polls.data.network

import com.example.votacion.features.polls.data.models.PollOutput
import com.google.gson.Gson
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PollSocketService @Inject constructor(
    private val socket: Socket,
    private val gson: Gson
) {
    private val _pollEvents = MutableSharedFlow<PollEvent>()
    val pollEvents: SharedFlow<PollEvent> = _pollEvents.asSharedFlow()

    init {
        setupListeners()
    }

    private fun setupListeners() {
        socket.on("poll_created") { args ->
            val data = args[0].toString()
            val poll = gson.fromJson(data, PollOutput::class.java)
            _pollEvents.tryEmit(PollEvent.PollCreated(poll))
        }

        socket.on("vote_cast") { args ->
            val data = args[0].toString()
            val poll = gson.fromJson(data, PollOutput::class.java)
            _pollEvents.tryEmit(PollEvent.VoteCast(poll))
        }

        socket.on("poll_deleted") { args ->
            val pollId = args[0].toString()
            _pollEvents.tryEmit(PollEvent.PollDeleted(pollId))
        }

        socket.connect()
    }

    fun disconnect() {
        socket.disconnect()
    }
}

sealed class PollEvent {
    data class PollCreated(val poll: PollOutput) : PollEvent()
    data class VoteCast(val poll: PollOutput) : PollEvent()
    data class PollDeleted(val pollId: String) : PollEvent()
}
