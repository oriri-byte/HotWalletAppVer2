package com.example.hotwalletappv2.domain.usecase

import com.example.hotwalletappv2.domain.repository.SmartContractRepository
import org.web3j.utils.Convert

class SendFundsUseCase(
    private val repository: SmartContractRepository
) {
    suspend operator fun invoke(toAddress: String, amountInEther: String): Result<String> {
        return try {
            val amountInWei = Convert.toWei(amountInEther, Convert.Unit.ETHER).toBigIntegerExact()
            repository.sendFunds(toAddress, amountInWei)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
