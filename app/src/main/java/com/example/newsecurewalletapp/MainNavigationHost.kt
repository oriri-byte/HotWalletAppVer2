package com.example.newsecurewalletapp

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.newsecurewalletapp.ui.screen.home.HomeScreen
import com.example.newsecurewalletapp.ui.screen.registration.RegistrationScreen
import com.example.newsecurewalletapp.ui.screen.send.SendScreen
import com.example.newsecurewalletapp.ui.viewmodel.SendViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

enum class SecureWalletScreen(@param:StringRes val title: Int) {
    Home(title = R.string.app_name),
    Registration(title = R.string.registration_title),
    Send(title = R.string.send_title)
}

@Composable
fun MainNavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = SecureWalletScreen.Home.name,
        modifier = modifier
    ) {
        composable(route = SecureWalletScreen.Home.name){
            HomeScreen(
                onNavigateToRegistration = {
                    navController.navigate(SecureWalletScreen.Registration.name)
                },
                onNavigateToSend = {
                    navController.navigate(SecureWalletScreen.Send.name)
                }
            )
        }

        composable(route = SecureWalletScreen.Registration.name) {
            RegistrationScreen()
        }

        composable(route = SecureWalletScreen.Send.name) {
            val sendViewModel: SendViewModel = viewModel()
            SendScreen(viewModel = sendViewModel)
        }
    }
}