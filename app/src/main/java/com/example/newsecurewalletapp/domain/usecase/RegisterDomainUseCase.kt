package com.example.newsecurewalletapp.domain.usecase

import com.example.newsecurewalletapp.domain.crypto.SignatureVerifier
import com.example.newsecurewalletapp.domain.model.DomainName
import com.example.newsecurewalletapp.domain.model.DomainRegistrationData
import com.example.newsecurewalletapp.domain.model.WalletAddress
import com.example.newsecurewalletapp.domain.repository.DomainRegistrationRepository

class RegisterDomainUseCase(
    private val repository: DomainRegistrationRepository,
    private val verifier: SignatureVerifier
) {
    suspend operator fun invoke(address: WalletAddress, domain: DomainName): DomainRegistrationData? {
        val signature = repository.fetchSignature(address, domain)
        val isValid = verifier.verifySignature(address, domain, signature)
        return if (isValid) {
            DomainRegistrationData(
                address = address,
                domain = domain,
                signature = signature
            )
        } else {
            null
        }
    }

}