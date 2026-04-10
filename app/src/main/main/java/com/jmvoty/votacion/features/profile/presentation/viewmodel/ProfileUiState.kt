package com.jmvoty.votacion.features.profile.presentation.viewmodel

import com.jmvoty.votacion.features.profile.domain.entities.UserProfile

data class ProfileUiState(
    val user: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdating: Boolean = false
)