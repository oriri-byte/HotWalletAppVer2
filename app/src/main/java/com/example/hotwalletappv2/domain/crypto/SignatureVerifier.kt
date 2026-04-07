package com.example.hotwalletappv2.domain.crypto

import com.example.hotwalletappv2.domain.model.DomainName
import com.example.hotwalletappv2.domain.model.ServerSignature
import com.example.hotwalletappv2.domain.model.WalletAddress

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