package com.example.hotwalletappv2.ui.viewmodel

import com.example.hotwalletappv2.domain.model.DomainRegistrationData

sealed interface SendUiState {
    object Idle : SendUiState
    object Resolving : SendUiState
    data class Resolved(val data: DomainRegistrationData) : SendUiState
    object Sending : SendUiState
    data class Success(val txHash: String) : SendUiState
    data class ReadyToTransfer(val data: DomainRegistrationData, val qrData: String) : SendUiState
    data class Error(val message: String) : SendUiState
}
