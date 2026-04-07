package com.example.newsecurewalletapp.domain.crypto

import com.example.newsecurewalletapp.domain.model.DomainName
import com.example.newsecurewalletapp.domain.model.ServerSignature
import com.example.newsecurewalletapp.domain.model.WalletAddress

interface SignatureVerifier {
    /**
     * 署名検証用のインターフェース
     */
    fun verifySignature(
        address: WalletAddress,
        domain: DomainName,
        signature: ServerSignature
    ): Boolean
}