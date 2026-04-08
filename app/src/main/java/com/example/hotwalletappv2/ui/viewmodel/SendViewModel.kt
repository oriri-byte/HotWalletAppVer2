package com.example.hotwalletappv2.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotwalletappv2.data.contract.SmartContractRepositoryImpl
import com.example.hotwalletappv2.domain.usecase.PrepareColdWalletTransferUseCase
import com.example.hotwalletappv2.domain.usecase.ResolveDomainUseCase
import kotlinx.coroutines.launch

class SendViewModel : ViewModel() {
    var domainName: String by mutableStateOf("")
    var amount: String by mutableStateOf("")
    var isScanning: Boolean by mutableStateOf(false)
    var uiState: SendUiState by mutableStateOf(SendUiState.Idle)
        private set
    private val smartContractRepository = SmartContractRepositoryImpl()
    private val resolveDomainUseCase by lazy { ResolveDomainUseCase(smartContractRepository) }
    private val prepareColdWalletTransferUseCase by lazy { PrepareColdWalletTransferUseCase(smartContractRepository) }

    fun resolve() {
        uiState = SendUiState.Resolving
        viewModelScope.launch {
            val result = resolveDomainUseCase(domainName)
            result.fold(
                onSuccess = { data ->
                    uiState = SendUiState.Resolved(data)
                },
                onFailure = { error ->
                    uiState = SendUiState.Error("ドメインの解析に失敗しました: ${error.message}")
                }
            )
        }
    }

    /**
     * コールドウォレット送信用データの作成（QRコード用）
     */
    fun prepareTransfer() {
        val currentState = uiState
        if (currentState !is SendUiState.Resolved || amount.isBlank()) return
        
        val data = currentState.data
        uiState = SendUiState.Sending // 遷移中
        viewModelScope.launch {
            val result = prepareColdWalletTransferUseCase(data, amount)
            result.fold(
                onSuccess = { qrData ->
                    uiState = SendUiState.ReadyToTransfer(data, qrData)
                },
                onFailure = { error ->
                    uiState = SendUiState.Error("トランザクションの作成に失敗しました: ${error.message}")
                }
            )
        }
    }



    fun broadcastSignedTransaction(signedTxHex: String) {
        uiState = SendUiState.Sending
        viewModelScope.launch {
            val broadcastSignedTransactionUseCase = com.example.hotwalletappv2.domain.usecase.BroadcastSignedTransactionUseCase(smartContractRepository)
            val result = broadcastSignedTransactionUseCase(signedTxHex)
            result.fold(
                onSuccess = { txHash ->
                    uiState = SendUiState.Success(txHash)
                },
                onFailure = { error ->
                    uiState = SendUiState.Error("トランザクション送信に失敗しました: ${error.message}")
                }
            )
        }
    }
}
