package com.example.hotwalletappv2.ui.screen.send

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hotwalletappv2.R
import com.example.hotwalletappv2.ui.viewmodel.SendUiState
import com.example.hotwalletappv2.ui.viewmodel.SendViewModel

@Composable
fun SendScreen(
    modifier: Modifier = Modifier, viewModel: SendViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { viewModel.domainName = it },
            label = { Text(stringResource(id = R.string.domain_label)) },
            value = viewModel.domainName
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.resolve() },
            enabled = viewModel.domainName.isNotBlank() && uiState !is SendUiState.Resolving
        ) {
            Text(text = stringResource(id = R.string.resolve_button))
        }

        Spacer(modifier = Modifier.height(16.dp))
        when (uiState) {
            is SendUiState.Resolving -> {
                Box(
                    modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            }

            is SendUiState.Resolved -> {
                Text(
                    text = "解決されたアドレス： ${uiState.address}"
                )
            }

            is SendUiState.Sending -> {
                Box(
                    modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is SendUiState.Success -> {
                Text(
                    text = stringResource(id = R.string.send_success_text, uiState.txHash),
                    color = Color(0xFF43A047)
                )
            }

            is SendUiState.Error -> {
                Text(
                    text = uiState.message, color = Color.Red
                )
            }

            else -> {}
        }


        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.amount,
            onValueChange = { viewModel.amount = it },
            label = { Text(stringResource(id = R.string.amount_label)) }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.sendFunds() },
            enabled = viewModel.resolvedAddress.isNotBlank() && viewModel.amount.isNotBlank() && uiState !is SendUiState.Sending
        ) {
            Text(text = stringResource(id = R.string.send_button))
        }

    }
}