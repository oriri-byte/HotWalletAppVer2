package com.example.newsecurewalletapp.domain.usecase

import com.example.newsecurewalletapp.domain.model.DomainRegistrationData
import com.example.newsecurewalletapp.domain.repository.SmartContractRepository

class SubmitTransactionUseCase(
    private val repository: SmartContractRepository
) {
    suspend operator fun invoke(transactionData: DomainRegistrationData): Result<String> {
        return repository.submitTransaction(transactionData)
    }
}