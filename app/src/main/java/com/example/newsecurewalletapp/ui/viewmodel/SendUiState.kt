package com.example.newsecurewalletapp.ui.viewmodel

sealed interface SendUiState {
    object Idle : SendUiState
    object Resolving : SendUiState
    data class Resolved(val address: String) : SendUiState
    object Sending : SendUiState
    data class Success(val txHash: String) : SendUiState
    data class Error(val message: String) : SendUiState
}
