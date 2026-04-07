package com.example.hotwalletappv2.domain.usecase

import com.example.hotwalletappv2.domain.crypto.SignatureVerifier
import com.example.hotwalletappv2.domain.model.DomainName
import com.example.hotwalletappv2.domain.model.DomainRegistrationData
import com.example.hotwalletappv2.domain.model.WalletAddress
import com.example.hotwalletappv2.domain.repository.DomainRegistrationRepository

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