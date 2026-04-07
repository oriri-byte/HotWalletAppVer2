package com.example.hotwalletappv2.domain.repository

import com.example.hotwalletappv2.domain.model.DomainName
import com.example.hotwalletappv2.domain.model.ServerSignature
import com.example.hotwalletappv2.domain.model.WalletAddress

interface DomainRegistrationRepository {
    suspend fun fetchSignature(address: WalletAddress, domain: DomainName): ServerSignature
}