package com.example.hotwalletappv2.domain.usecase

import com.example.hotwalletappv2.domain.model.DomainRegistrationData
import org.json.JSONObject

/**
 * コールドウォレットへ転送するためのJSONデータを生成するユースケース
 */
class CreateColdWalletTransferDataUseCase {

    /**
     * 送金に必要な情報をJSON形式にシリアライズする
     *
     * @param data ドメイン登録データ（アドレス、ドメイン、署名）
     * @param rawTxHex 未署名のRawTransaction（RLPエンコード済み）
     * @return シリアライズされたJSON文字列
     */
    operator fun invoke(
        data: DomainRegistrationData,
        rawTxHex: String
    ): String {
        val payload = JSONObject().apply {
            put("tx", rawTxHex)
            put("domain", data.domain.value)
            put("sig", data.signature.value)
        }
        return payload.toString()
    }
}
