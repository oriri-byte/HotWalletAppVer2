package com.example.hotwalletappv2.data.contract

import com.example.hotwalletappv2.BuildConfig
import com.example.hotwalletappv2.domain.model.DomainName
import com.example.hotwalletappv2.domain.model.DomainRegistrationData
import com.example.hotwalletappv2.domain.model.ServerSignature
import com.example.hotwalletappv2.domain.model.WalletAddress
import com.example.hotwalletappv2.domain.repository.SmartContractRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Numeric
import java.math.BigInteger
import kotlin.collections.emptyList

class SmartContractRepositoryImpl : SmartContractRepository {
    private val contractAddress: String = BuildConfig.CONTRACT_ADDRESS
    private val walletAddress: String = BuildConfig.WALLET_ADDRESS
    private val walletAddress2: String = BuildConfig.WALLET_ADDRESS2
    private val infuraUrl: String = "https://sepolia.infura.io/v3/" + BuildConfig.INFURA_API_KEY
    private val credentials = Credentials.create(BuildConfig.PRIVATE_KEY)
    private val web3j: Web3j = Web3j.build(HttpService(infuraUrl))

    override suspend fun submitTransaction(transactionData: DomainRegistrationData): Result<String> =
        withContext(Dispatchers.IO) {
            val ethGetTransactionCount = web3j.ethGetTransactionCount(
                walletAddress, DefaultBlockParameterName.LATEST
            ).sendAsync().get()
            val nonce: BigInteger = ethGetTransactionCount.transactionCount

            val gasPrice: BigInteger = web3j.ethGasPrice().send().gasPrice
            val gasLimit: BigInteger = BigInteger.valueOf(300000)
            val function = Function(
                "register", listOf(
                    Utf8String(transactionData.domain.value),
                    Utf8String(transactionData.address.value),
                    Utf8String(transactionData.signature.value)
                ), emptyList()
            )
            val encodedFunction = FunctionEncoder.encode(function)
            val rawTransaction: RawTransaction = RawTransaction.createTransaction(
                nonce, gasPrice, gasLimit, contractAddress, BigInteger.ZERO, encodedFunction
            )

            val chainId = 11155111L
            val signedMessage =
                TransactionEncoder.signMessage(rawTransaction, chainId, credentials)
            val hexValue = Numeric.toHexString(signedMessage)
            val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get()
            val transactionHash: String = ethSendTransaction.transactionHash
            Result.success(transactionHash)
        }

    override suspend fun resolveDomain(domain: String): Result<DomainRegistrationData> =
        withContext(Dispatchers.IO) {
            try {
                val simpleENS = SimpleENS.load(
                    contractAddress,
                    web3j,
                    credentials,
                    DefaultGasProvider()
                )
                val tuple = simpleENS.resolve(domain).send()
                val address = tuple.component1()
                val signature = tuple.component2()
                if (address.isNullOrBlank() || address == "0x0000000000000000000000000000000000000000") {
                    Result.failure(Exception("Domain not registered"))
                } else {
                    Result.success(
                        DomainRegistrationData(
                            address = WalletAddress(address),
                            domain = DomainName(domain),
                            signature = ServerSignature(signature)
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun createUnsignedTransaction(
        toAddress: String,
        amountInWei: BigInteger
    ): Result<RawTransaction> =
        withContext(Dispatchers.IO) {
            try {
                val ethGetTransactionCount = web3j.ethGetTransactionCount(
                    walletAddress2, DefaultBlockParameterName.LATEST
                ).sendAsync().get()
                val nonce: BigInteger = ethGetTransactionCount.transactionCount

                val gasPrice: BigInteger = web3j.ethGasPrice().send().gasPrice
                val gasLimit: BigInteger = BigInteger.valueOf(21000)
                val chainId = 11155111L // Sepolia

                val rawTransaction = RawTransaction.createEtherTransaction(
                    chainId,
                    nonce,
                    gasLimit,
                    toAddress,
                    amountInWei,
                    BigInteger.valueOf(1500000000L), // maxPriorityFeePerGas (1.5 Gwei)
                    gasPrice // maxFeePerGas
                )
                Result.success(rawTransaction)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun broadcastTransaction(signedTransactionHex: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val hexWithPrefix = if (signedTransactionHex.startsWith("0x")) signedTransactionHex else "0x$signedTransactionHex"
                val ethSendTransaction = web3j.ethSendRawTransaction(hexWithPrefix).sendAsync().get()
                if (ethSendTransaction.hasError()) {
                    Result.failure(Exception(ethSendTransaction.error.message))
                } else {
                    Result.success(ethSendTransaction.transactionHash)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}