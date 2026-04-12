package com.jmvoty.votacion.features.polls.data.network

import com.jmvoty.votacion.features.polls.data.models.*
import okhttp3.MultipartBody
import retrofit2.http.*
import retrofit2.Response

interface PollService {
    @GET("polls")
    suspend fun listPolls(): List<PollOutput>


    @Multipart
    @POST("polls")
    suspend fun createPollWithImages(
        @Query("title") title: String,
        @Query("options") options: String,
        @Part images: List<MultipartBody.Part>
    ): Response<Unit>

    @POST("polls")
    suspend fun createPollSimple(
        @Query("title") title: String,
        @Query("options") options: String
    ): Response<Unit>

    @GET("polls/{id}")
    suspend fun getPoll(@Path("id") id: String): PollOutput

    @PUT("polls/{id}")
    suspend fun updatePoll(
        @Path("id") id: String,
        @Body request: UpdatePollRequest
    ): Response<PollOutput>

    @DELETE("polls/{id}")
    suspend fun deletePoll(@Path("id") id: String): Unit

    @POST("polls/{poll_id}/vote/{option_id}")
    suspend fun castVote(
        @Path("poll_id") pollId: String,
        @Path("option_id") optionId: String
    ): Unit
}
