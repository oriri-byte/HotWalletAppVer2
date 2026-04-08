package com.example.hotwalletappv2.domain.usecase

import com.example.hotwalletappv2.domain.repository.SmartContractRepository

class BroadcastSignedTransactionUseCase(
    private val smartContractRepository: SmartContractRepository
) {
    suspend operator fun invoke(signedTransactionHex: String): Result<String> {
        return smartContractRepository.broadcastTransaction(signedTransactionHex)
    }
}
