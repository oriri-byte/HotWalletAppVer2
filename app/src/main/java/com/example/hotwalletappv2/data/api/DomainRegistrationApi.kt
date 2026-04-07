package com.example.hotwalletappv2.data.api

import retrofit2.http.Body
import retrofit2.http.POST

data class SignatureRequest(
    val address: String,
    val domain: String
)

data class SignatureResponse(
    val signature: String
)

interface DomainRegistrationApi {
    @POST("/api/v1/sign")
    suspend fun fetchSignature(@Body request: SignatureRequest): SignatureResponse
}
