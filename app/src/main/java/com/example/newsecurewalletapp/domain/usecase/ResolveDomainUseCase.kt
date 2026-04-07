package com.example.newsecurewalletapp.domain.usecase

import com.example.newsecurewalletapp.domain.repository.SmartContractRepository

class ResolveDomainUseCase(
    private val repository: SmartContractRepository
) {
    suspend operator fun invoke(domain: String): Result<String> {
        return repository.resolveDomain(domain)
    }
}
