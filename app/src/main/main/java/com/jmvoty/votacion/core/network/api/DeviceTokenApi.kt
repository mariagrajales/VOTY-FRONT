package com.jmvoty.votacion.core.network.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DeviceTokenApi {
    @POST("user/device-token")
    suspend fun sendDeviceToken(@Body request: DeviceTokenRequest): Response<Unit>
}

data class DeviceTokenRequest(val token: String)