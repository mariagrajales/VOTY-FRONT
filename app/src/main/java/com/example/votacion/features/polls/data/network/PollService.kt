package com.example.votacion.features.polls.data.network

import com.example.votacion.features.polls.data.models.*
import retrofit2.http.*

interface PollService {
    @GET("polls")
    suspend fun listPolls(): List<PollOutput>

    @POST("polls")
    suspend fun createPoll(@Body request: CreatePollRequest): PollOutput?

    @GET("polls/{id}")
    suspend fun getPoll(@Path("id") id: String): PollOutput

    @PUT("polls/{id}")
    suspend fun updatePoll(
        @Path("id") id: String,
        @Body request: Map<String, Any>
    ): PollOutput?

    @DELETE("polls/{id}")
    suspend fun deletePoll(@Path("id") id: String): Unit

    @POST("polls/{poll_id}/vote/{option_id}")
    suspend fun castVote(
        @Path("poll_id") pollId: String,
        @Path("option_id") optionId: String
    ): Unit
}
