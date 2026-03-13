package com.example.votacion.features.profile.domain.entities

data class UserProfile(
    val id: String,
    val email: String,
    val name: String,
    val memberSince: String, // Fecha ya formateada
    val avatarImage: String?
)