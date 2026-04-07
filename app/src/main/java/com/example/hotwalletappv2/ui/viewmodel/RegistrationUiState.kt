package com.example.hotwalletappv2.ui.viewmodel

import com.example.hotwalletappv2.domain.model.DomainRegistrationData

sealed interface RegistrationUiState {
    object Idle : RegistrationUiState
    object Loading : RegistrationUiState
    object Submitting : RegistrationUiState
    data class TransactionComplete(val txHash: String) : RegistrationUiState
    data class Success(val transactionData: DomainRegistrationData) : RegistrationUiState
    data class Error(val message: String) : RegistrationUiState
}

