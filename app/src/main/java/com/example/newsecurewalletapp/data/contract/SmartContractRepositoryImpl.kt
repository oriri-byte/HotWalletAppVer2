package com.example.newsecurewalletapp.data.contract

import com.example.newsecurewalletapp.BuildConfig
import com.example.newsecurewalletapp.domain.model.DomainRegistrationData
import com.example.newsecurewalletapp.domain.repository.SmartContractRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
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

    override suspend fun resolveDomain(domain: String): Result<String> =
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
                if (address.isNullOrBlank() || address == "0x0000000000000000000000000000000000000000") {
                    Result.failure(Exception("Domain not registered"))
                } else {
                    Result.success(address)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun sendFunds(toAddress: String, amountInWei: BigInteger): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val credentials2 = Credentials.create(BuildConfig.WALLET_PRIVATE_KEY2)
                val ethGetTransactionCount = web3j.ethGetTransactionCount(
                    walletAddress2, DefaultBlockParameterName.LATEST
                ).sendAsync().get()
                val nonce = ethGetTransactionCount.transactionCount

                val gasPrice = web3j.ethGasPrice().send().gasPrice
                val gasLimit = BigInteger.valueOf(21000) // Standard Ether transfer gas limit

                val rawTransaction = RawTransaction.createEtherTransaction(
                    nonce, gasPrice, gasLimit, toAddress, amountInWei
                )

                val chainId = 11155111L
                val signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials2)
                val hexValue = Numeric.toHexString(signedMessage)

                val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get()
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