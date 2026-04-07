package com.example.hotwalletappv2.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotwalletappv2.BuildConfig
import com.example.hotwalletappv2.data.contract.SmartContractRepositoryImpl
import com.example.hotwalletappv2.data.repository.DomainRegistrationRepositoryImpl
import com.example.hotwalletappv2.data.security.EcdsaSignatureVerifier
import com.example.hotwalletappv2.domain.model.DomainName
import com.example.hotwalletappv2.domain.model.DomainRegistrationData
import com.example.hotwalletappv2.domain.model.WalletAddress
import com.example.hotwalletappv2.domain.usecase.RegisterDomainUseCase
import com.example.hotwalletappv2.domain.usecase.SubmitTransactionUseCase
import kotlinx.coroutines.launch


class RegistrationViewModel : ViewModel() {
    var walletAddress by mutableStateOf(BuildConfig.WALLET_ADDRESS)
    var domainName by mutableStateOf("")
    var pendingTransactionData: DomainRegistrationData? by mutableStateOf(null)
        private set
    var uiState: RegistrationUiState by mutableStateOf(RegistrationUiState.Idle)
        private set
    private val registerDomainUseCase = RegisterDomainUseCase(
        repository = DomainRegistrationRepositoryImpl(),
        verifier = EcdsaSignatureVerifier()
    )
    private val submitTransactionUseCase = SubmitTransactionUseCase(
        repository = SmartContractRepositoryImpl()
    )

    fun requestSignature() {
        uiState = RegistrationUiState.Loading
        viewModelScope.launch {
            try {
                val transactionData = registerDomainUseCase(
                    WalletAddress(walletAddress),
                    DomainName(domainName)
                )
                if (transactionData != null) {
                    uiState = RegistrationUiState.Success(transactionData)
                    pendingTransactionData = transactionData
                } else {
                    uiState = RegistrationUiState.Error("署名検証に失敗しました．")
                }
            } catch (e: Exception) {
                uiState =
                    RegistrationUiState.Error("署名リクエスト中にエラーが発生しました．${e.message}")
            }
        }
    }

    fun submitTransaction() {
        uiState = RegistrationUiState.Submitting
        val data = pendingTransactionData ?: return
        viewModelScope.launch {
            val result = submitTransactionUseCase(data)
            result.fold(
                onSuccess = { txHash ->
                    uiState = RegistrationUiState.TransactionComplete(txHash)
                },
                onFailure = { error ->
                    uiState =
                        RegistrationUiState.Error("トランザクション送信中にエラーが発生しました．${error.message}")
                }
            )
        }
    }
}
