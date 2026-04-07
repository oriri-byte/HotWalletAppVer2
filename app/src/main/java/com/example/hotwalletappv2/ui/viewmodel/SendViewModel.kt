package com.example.hotwalletappv2.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotwalletappv2.data.contract.SmartContractRepositoryImpl
import com.example.hotwalletappv2.domain.usecase.ResolveDomainUseCase
import com.example.hotwalletappv2.domain.usecase.SendFundsUseCase
import kotlinx.coroutines.launch

class SendViewModel : ViewModel(

) {
    var domainName: String by mutableStateOf("")
    var resolvedAddress: String by mutableStateOf("")
    var amount: String by mutableStateOf("")
    var uiState: SendUiState by mutableStateOf(SendUiState.Idle)
        private set
    private val smartContractRepository = SmartContractRepositoryImpl()
    private val resolveDomainUseCase = ResolveDomainUseCase(smartContractRepository)
    private val sendFundsUseCase = SendFundsUseCase(smartContractRepository)

    fun resolve() {
        uiState = SendUiState.Resolving
        viewModelScope.launch {
            val result = resolveDomainUseCase(domainName)
            result.fold(
                onSuccess = { address ->
                    resolvedAddress = address
                    uiState = SendUiState.Resolved(address)
                },
                onFailure = { error ->
                    uiState = SendUiState.Error("ドメインの解析に失敗しました: ${error.message}")
                }
            )
        }
    }

    fun sendFunds() {
        if (resolvedAddress.isBlank() || amount.isBlank()) return
        uiState = SendUiState.Sending
        viewModelScope.launch {
            val result = sendFundsUseCase(resolvedAddress, amount)
            result.fold(
                onSuccess = { txHash ->
                    uiState = SendUiState.Success(txHash)
                },
                onFailure = { error ->
                    uiState = SendUiState.Error("送金に失敗しました: ${error.message}")
                }
            )
        }

    }
}
