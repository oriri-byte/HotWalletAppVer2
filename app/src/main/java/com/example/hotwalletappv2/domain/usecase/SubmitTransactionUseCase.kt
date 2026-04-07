package com.example.hotwalletappv2.domain.usecase

import com.example.hotwalletappv2.domain.model.DomainRegistrationData
import com.example.hotwalletappv2.domain.repository.SmartContractRepository

class SubmitTransactionUseCase(
    private val repository: SmartContractRepository
) {
    suspend operator fun invoke(transactionData: DomainRegistrationData): Result<String> {
        return repository.submitTransaction(transactionData)
    }
}