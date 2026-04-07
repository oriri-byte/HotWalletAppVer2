package com.example.hotwalletappv2.data.security

import com.example.hotwalletappv2.BuildConfig
import com.example.hotwalletappv2.domain.crypto.SignatureVerifier
import com.example.hotwalletappv2.domain.model.DomainName
import com.example.hotwalletappv2.domain.model.ServerSignature
import com.example.hotwalletappv2.domain.model.WalletAddress
import org.web3j.crypto.ECDSASignature
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.util.Arrays

class EcdsaSignatureVerifier(
    private val expectedServerAddress: String = BuildConfig.SERVER_ADDRESS
) : SignatureVerifier {
    override fun verifySignature(
        address: WalletAddress,
        domain: DomainName,
        signature: ServerSignature
    ): Boolean {
        // TODO:実際の検証ロジックを実装
        val message = "${domain.value}${address.value}"
        val msgHash = Sign.getEthereumMessageHash(message.toByteArray(Charsets.UTF_8))
        val signatureBytes = Numeric.hexStringToByteArray(signature.value)

        if (signatureBytes.size != 65) return false
        val v = signatureBytes[64]

        val rBytes = Arrays.copyOfRange(signatureBytes, 0, 32)
        val sBytes = Arrays.copyOfRange(signatureBytes, 32, 64)

        val ecdsaSignature = ECDSASignature(BigInteger(1, rBytes), BigInteger(1, sBytes))

        var match: Boolean = false
        for (i in 0..3) {
            val publicKey: BigInteger? = Sign.recoverFromSignature(
                i,
                ecdsaSignature,
                msgHash
            )
            if (publicKey != null) {
                val addressRecovered = "0x" + Keys.getAddress(publicKey)

                println("DEBUG: Expected: \n\t$expectedServerAddress, \nRecovered: \t$addressRecovered")
                if (addressRecovered.equals(expectedServerAddress, ignoreCase = true)) {
                    match = true
                    break
                }
            }
        }

        return match
    }
}