package com.example.hotwalletappv2.domain.model

//ウォレットアドレスを表す型
@JvmInline
value class WalletAddress(val value: String) {
    init {
        require(value.isNotBlank()) {
            "WalletAddress cannot be blank"
        }
    }
}

//ドメイン名を表す型
@JvmInline
value class DomainName(val value: String) {
    init {
        require(value.isNotBlank()) {
            "DomainName cannot be blank"
        }
    }
}

//署名を表す型
@JvmInline
value class ServerSignature(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Signature cannot be blank"
        }
    }
}