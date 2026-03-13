package com.example.votacion.features.profile.data.mapper // <--- REVISA ESTO

import com.example.votacion.features.profile.data.models.ProfileResponse
import com.example.votacion.features.profile.domain.entities.UserProfile

fun ProfileResponse.toDomain(): UserProfile {
    return UserProfile(
        id = this.id,
        email = this.email,
        name = this.name,
        memberSince = this.createdAt.take(10),
        avatarImage = this.avatarImage
    )
}