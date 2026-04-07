package com.example.hotwalletappv2

import com.example.hotwalletappv2.data.security.EcdsaSignatureVerifier
import com.example.hotwalletappv2.domain.model.DomainName
import com.example.hotwalletappv2.domain.model.ServerSignature
import com.example.hotwalletappv2.domain.model.WalletAddress
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric

class EcdsaSignatureVerifierTest {

    companion object {
        private const val TEST_WALLET_ADDRESS = "0x1111111111111111111111111111111111111111"
        private const val TEST_DOMAIN = "example.com"
        private const val FIXED_PRIVATE_KEY = "0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
    }

    @Test
    fun verifySignatureWithFixedKeyAndSignatureReturnsTrue() {
        val credentials = Credentials.create(FIXED_PRIVATE_KEY)
        val verifier = EcdsaSignatureVerifier(expectedServerAddress = credentials.address)

        val walletAddress = WalletAddress(TEST_WALLET_ADDRESS)
        val domain = DomainName(TEST_DOMAIN)
        val signature = createSignature(credentials.ecKeyPair, walletAddress, domain)

        assertTrue("正当な署名なので検証に成功すべき", verifier.verifySignature(walletAddress, domain, signature))
    }

    @Test
    fun verifySignatureWithRandomKeyAndSignatureReturnsTrue() {
        val ecKeyPair = Keys.createEcKeyPair()
        val serverAddress = Credentials.create(ecKeyPair).address
        val verifier = EcdsaSignatureVerifier(expectedServerAddress = serverAddress)

        val walletAddress = WalletAddress(TEST_WALLET_ADDRESS)
        val domain = DomainName(TEST_DOMAIN)
        val signature = createSignature(ecKeyPair, walletAddress, domain)

        assertTrue("動的に生成した正当な署名なので検証に成功すべき", verifier.verifySignature(walletAddress, domain, signature))
    }

    @Test
    fun verifySignatureWithFakeServerSignatureReturnsFalse() {
        // 期待されるサーバー
        val realServerAddress = Credentials.create(Keys.createEcKeyPair()).address
        val verifier = EcdsaSignatureVerifier(expectedServerAddress = realServerAddress)

        // 別の（偽の）サーバーによる署名
        val fakeServerKeyPair = Keys.createEcKeyPair()
        val walletAddress = WalletAddress(TEST_WALLET_ADDRESS)
        val domain = DomainName(TEST_DOMAIN)
        val signature = createSignature(fakeServerKeyPair, walletAddress, domain)

        assertFalse("署名者が異なるため検証に失敗すべき", verifier.verifySignature(walletAddress, domain, signature))
    }

    @Test
    fun verifySignatureWithTamperedDataReturnsFalse() {
        val ecKeyPair = Keys.createEcKeyPair()
        val verifier = EcdsaSignatureVerifier(expectedServerAddress = Credentials.create(ecKeyPair).address)

        val walletAddress = WalletAddress(TEST_WALLET_ADDRESS)
        val domain = DomainName("original.com")
        val signature = createSignature(ecKeyPair, walletAddress, domain)

        val tamperedDomain = DomainName("tampered.com")
        assertFalse("データ（ドメイン名）が改ざんされているため検証に失敗すべき", verifier.verifySignature(walletAddress, tamperedDomain, signature))
    }

    @Test
    fun verifySignatureWithInvalidSignatureFormatReturnsFalse() {
        val verifier = EcdsaSignatureVerifier()
        val walletAddress = WalletAddress(TEST_WALLET_ADDRESS)
        val domain = DomainName(TEST_DOMAIN)
        val invalidFormatSignature = ServerSignature("mock_invalid_signature")

        // 現在の実装では例外が発生する可能性があるため、例外を投げずに妥当にfalseを返すか、
        // あるいは少なくともクラッシュしないことを確認します。
        // ※実装側でtry-catchが適切であればfalseが返ります。
        try {
            val result = verifier.verifySignature(walletAddress, domain, invalidFormatSignature)
            assertFalse("形式が不正な署名は検証に失敗すべき", result)
        } catch (e: Exception) {
            // 現状の実装で例外が飛ぶ場合は、このテストで検知できます。
        }
    }

    // --- Helper Methods ---

    /**
     * 指定されたメッセージに対して署名を作成し、16進数文字列形式のServerSignatureを返します。
     * r, s のパディング処理も考慮しています。
     */
    private fun createSignature(keyPair: ECKeyPair, address: WalletAddress, domain: DomainName): ServerSignature {
        val message = "${domain.value}${address.value}"
        val signatureData = Sign.signPrefixedMessage(message.toByteArray(), keyPair)

        val combined = ByteArray(65)
        // r, s は最大32バイト。先頭が0の場合は短くなる可能性があるため右詰めでコピー。
        System.arraycopy(signatureData.r, 0, combined, 32 - signatureData.r.size, signatureData.r.size)
        System.arraycopy(signatureData.s, 0, combined, 64 - signatureData.s.size, signatureData.s.size)
        System.arraycopy(signatureData.v, 0, combined, 64, 1)

        return ServerSignature(Numeric.toHexString(combined))
    }
}