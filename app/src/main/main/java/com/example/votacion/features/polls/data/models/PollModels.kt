package com.example.votacion.features.polls.data.models

import com.google.gson.annotations.SerializedName

data class PollOutput(
    val id: String,
    val title: String,
    val options: List<OptionOutput>,
    val voted: Boolean,
    @SerializedName("selected_option_id")
    val selectedOptionId: String? = null,
    @SerializedName("is_open")
    val isOpen: Boolean
){
    val hasVotes: Boolean get() = options.any { it.votesCount > 0 }
}

data class OptionOutput(
    val id: String,
    val text: String,
    @SerializedName("votes_count")
    val votesCount: Int
)

data class CreatePollRequest(
    val title: String,
    val options: List<String>
)

data class UpdatePollRequest(
    val title: String,
    @SerializedName("is_open")
    val isOpen: Boolean,
    val options: List<String>? = null
)
