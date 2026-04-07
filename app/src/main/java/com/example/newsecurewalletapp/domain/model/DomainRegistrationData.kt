package com.example.newsecurewalletapp.domain.model

/**
 * スマートコントラクトへのドメイン登録トランザクションに必要なデータを保持するクラス
 *
 * @param address 登録対象のウォレットアドレス
 * @param domain 登録するドメイン名
 * @param signature 登録時の署名
 */
data class DomainRegistrationData(
    val address: WalletAddress,
    val domain: DomainName,
    val signature: ServerSignature
)
