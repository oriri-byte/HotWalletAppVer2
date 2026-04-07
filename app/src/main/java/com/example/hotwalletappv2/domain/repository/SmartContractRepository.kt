package com.example.hotwalletappv2.domain.repository

import com.example.hotwalletappv2.domain.model.DomainRegistrationData

interface SmartContractRepository {
    suspend fun submitTransaction(transactionData: DomainRegistrationData): Result<String>
    suspend fun resolveDomain(domain: String): Result<String>
    suspend fun sendFunds(toAddress: String, amountInWei: java.math.BigInteger): Result<String>
}