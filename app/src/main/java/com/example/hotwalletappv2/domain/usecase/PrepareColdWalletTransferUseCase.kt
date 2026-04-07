package com.example.hotwalletappv2.domain.usecase

import com.example.hotwalletappv2.domain.model.DomainRegistrationData
import com.example.hotwalletappv2.domain.repository.SmartContractRepository
import org.web3j.crypto.TransactionEncoder
import org.web3j.utils.Convert
import org.web3j.utils.Numeric

/**
 * コールドウォレットへ転送するための未署名トランザクションデータを準備するユースケース
 * （ドメイン解決結果と送金額を受け取り、QR表示用のシリアライズ済みデータを返す）
 */
class PrepareColdWalletTransferUseCase(
    private val repository: SmartContractRepository,
    private val formatter: CreateColdWalletTransferDataUseCase = CreateColdWalletTransferDataUseCase()
) {
    /**
     * 送金に必要な情報を準備し、JSON形式にシリアライズする
     *
     * @param data ドメイン登録データ（宛先情報）
     * @param amountEther 送金額（Ether単位）
     * @return シリアライズされたJSON文字列
     */
    suspend operator fun invoke(
        data: DomainRegistrationData,
        amountEther: String
    ): Result<String> {
        return try {
            // 1. Weiに変換
            val amountInWei = Convert.toWei(amountEther, Convert.Unit.ETHER).toBigIntegerExact()

            // 2. インフラ層（Repository）から未署名のRawTransaction（nonce, gas等を含む）を取得
            val result = repository.createUnsignedTransaction(data.address.value, amountInWei)
            
            result.map { rawTx ->
                // 3. RLPエンコード（未署名フォーマット）
                val rawTxHex = Numeric.toHexString(TransactionEncoder.encode(rawTx))

                // 4. フォーマッターを使って最終的なJSON文字列を作成
                formatter(data, rawTxHex)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
