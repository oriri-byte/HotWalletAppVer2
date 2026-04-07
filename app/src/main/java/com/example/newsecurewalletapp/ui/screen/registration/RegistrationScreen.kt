package com.example.newsecurewalletapp.ui.screen.registration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsecurewalletapp.ui.viewmodel.RegistrationUiState
import com.example.newsecurewalletapp.ui.viewmodel.RegistrationViewModel
import androidx.compose.ui.res.stringResource
import com.example.newsecurewalletapp.R

@Composable
fun RegistrationScreen(
    modifier: Modifier = Modifier,
    viewModel: RegistrationViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    Column(modifier = modifier.padding(16.dp)) {
        TextField(
            value = viewModel.walletAddress,
            onValueChange = { viewModel.walletAddress = it },
            label = { Text(stringResource(id = R.string.wallet_address_label)) }
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextField(
            value = viewModel.domainName,
            onValueChange = { viewModel.domainName = it },
            label = { Text(stringResource(id = R.string.domain_label)) }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.requestSignature() },
            enabled = viewModel.walletAddress.isNotBlank() && viewModel.domainName.isNotBlank() && uiState !is RegistrationUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (uiState is RegistrationUiState.Loading) stringResource(id = R.string.loading_text) else stringResource(
                    id = R.string.request_signature_button
                )
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState is RegistrationUiState.Success,
            onClick = {
                viewModel.submitTransaction()
            }
        ) {
            Text(
                text = stringResource(id = R.string.transaction_send_button)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        // TODO:uiStateが変更されるたびにデータが消えてしまう． `TransactionData`を保持する仕組みが必要
        when (uiState) {
            is RegistrationUiState.Idle -> {

            }

            is RegistrationUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is RegistrationUiState.Submitting -> {

            }

            is RegistrationUiState.TransactionComplete -> {
                Text(
                    text = stringResource(id = R.string.registration_complete_text, uiState.txHash),
                    color = Blue
                )
            }

            is RegistrationUiState.Success -> {
                viewModel.pendingTransactionData?.let { data ->
                    Text(
                        text = stringResource(
                            id = R.string.signature_success_text,
                            data.signature
                        ),
                        color = Green
                    )
                }
            }

            is RegistrationUiState.Error -> {
                Text(
                    text = uiState.message,
                    color = Red
                )
            }
        }

    }
}