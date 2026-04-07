package com.example.hotwalletappv2.data.repository

import com.example.hotwalletappv2.data.api.RetrofitClient
import com.example.hotwalletappv2.data.api.SignatureRequest
import com.example.hotwalletappv2.domain.model.DomainName
import com.example.hotwalletappv2.domain.model.ServerSignature
import com.example.hotwalletappv2.domain.model.WalletAddress
import com.example.hotwalletappv2.domain.repository.DomainRegistrationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DomainRegistrationRepositoryImpl : DomainRegistrationRepository {
    private val api = RetrofitClient.api

    override suspend fun fetchSignature(address: WalletAddress, domain: DomainName): ServerSignature {
        return withContext(Dispatchers.IO) {
            try {
                val request = SignatureRequest(
                    address = address.value,
                    domain = domain.value
                )
                val response = api.fetchSignature(request)
                ServerSignature(response.signature)
            } catch (e: Exception) {
                throw RuntimeException("サーバーから署名を取得できませんでした: ${e.message}", e)
            }
        }
    }
}