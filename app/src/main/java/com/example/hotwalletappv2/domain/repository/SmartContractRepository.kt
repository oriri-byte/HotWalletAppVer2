package com.example.hotwalletappv2.domain.repository

import com.example.hotwalletappv2.domain.model.DomainRegistrationData
import org.web3j.crypto.RawTransaction
import java.math.BigInteger

interface SmartContractRepository {
    suspend fun submitTransaction(transactionData: DomainRegistrationData): Result<String>
    suspend fun resolveDomain(domain: String): Result<DomainRegistrationData>
    suspend fun createUnsignedTransaction(toAddress: String, amountInWei: BigInteger): Result<RawTransaction>
    suspend fun broadcastTransaction(signedTransactionHex: String): Result<String>
}