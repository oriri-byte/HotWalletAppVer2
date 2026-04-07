package com.example.hotwalletappv2.domain.usecase

import com.example.hotwalletappv2.domain.repository.SmartContractRepository

class ResolveDomainUseCase(
    private val repository: SmartContractRepository
) {
    suspend operator fun invoke(domain: String): Result<String> {
        return repository.resolveDomain(domain)
    }
}
