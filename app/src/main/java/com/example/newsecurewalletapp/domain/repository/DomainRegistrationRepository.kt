package com.example.newsecurewalletapp.domain.repository

import com.example.newsecurewalletapp.domain.model.DomainName
import com.example.newsecurewalletapp.domain.model.ServerSignature
import com.example.newsecurewalletapp.domain.model.WalletAddress

interface DomainRegistrationRepository {
    suspend fun fetchSignature(address: WalletAddress, domain: DomainName): ServerSignature
}