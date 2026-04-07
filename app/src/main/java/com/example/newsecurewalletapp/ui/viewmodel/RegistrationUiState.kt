package com.example.newsecurewalletapp.ui.viewmodel

import com.example.newsecurewalletapp.domain.model.DomainRegistrationData

sealed interface RegistrationUiState {
    object Idle : RegistrationUiState
    object Loading : RegistrationUiState
    object Submitting : RegistrationUiState
    data class TransactionComplete(val txHash: String) : RegistrationUiState
    data class Success(val transactionData: DomainRegistrationData) : RegistrationUiState
    data class Error(val message: String) : RegistrationUiState
}

