package org.delcom.pam_p5_ifs23051.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.delcom.pam_p5_ifs23051.helper.ConstHelper
import org.delcom.pam_p5_ifs23051.helper.RouteHelper
import org.delcom.pam_p5_ifs23051.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23051.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23051.ui.components.StatusCard
import org.delcom.pam_p5_ifs23051.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23051.ui.components.TopAppBarMenuItem
import org.delcom.pam_p5_ifs23051.ui.theme.DelcomTheme
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthActionUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthLogoutUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel
) {
    // Ambil data dari viewmodel
    val uiStateAuth by authViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var isFreshToken by remember { mutableStateOf(false) }
    var authToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (isLoading) return@LaunchedEffect

        isLoading = true
        isFreshToken = true

        uiStateAuth.authLogout = AuthLogoutUIState.Loading

        authViewModel.loadTokenFromPreferences()
    }

    fun onLogout(token: String){
        isLoading = true
        authViewModel.logout(token)
    }

    LaunchedEffect(uiStateAuth.auth) {
        if (!isLoading) {
            return@LaunchedEffect
        }

        if (uiStateAuth.auth !is AuthUIState.Loading) {
            if (uiStateAuth.auth is AuthUIState.Success) {
                if (isFreshToken) {
                    val dataToken = (uiStateAuth.auth as AuthUIState.Success).data
                    authViewModel.refreshToken(dataToken.authToken, dataToken.refreshToken)
                    isFreshToken = false
                } else if(uiStateAuth.authRefreshToken is AuthActionUIState.Success) {
                    val newToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
                    if (authToken != newToken) {
                        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
                    }
                    isLoading = false
                }
            } else {
                onLogout("")
            }
        }
    }

    LaunchedEffect(uiStateAuth.authLogout) {
        if (uiStateAuth.authLogout !is AuthLogoutUIState.Loading) {
            RouteHelper.to(
                navController,
                ConstHelper.RouteNames.AuthLogin.path,
                true
            )
        }
    }

    if (isLoading || authToken == null || isFreshToken) {
        LoadingUI()
        return
    }

    // Menu Top App Bar
    val menuItems = listOf(
        TopAppBarMenuItem(
            text = "Profile",
            icon = Icons.Filled.Person,
            route = ConstHelper.RouteNames.Profile.path
        ),
        TopAppBarMenuItem(
            text = "Logout",
            icon = Icons.AutoMirrored.Filled.Logout,
            route = null,
            onClick = {
                onLogout(authToken ?: "")
            }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBarComponent(
            navController = navController,
            title = "Home",
            showBackButton = false,
            customMenuItems = menuItems
        )
        // Content
        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            HomeUI()
        }
        // Bottom Nav
        BottomNavComponent(navController = navController)
    }
}

@Composable
fun HomeUI() {
    val totalTodos = 0
    val doneTodos = 0
    val pendingTodos = 0

    Column(
        modifier = Modifier.padding(top = 16.dp)
    )
    {
        // Header App
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = "📋 My Todos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Status Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            // Total Todos
            StatusCard(
                title = "Total",
                value = totalTodos.toString(),
                icon = Icons.AutoMirrored.Filled.List
            )

            // Selesai
            StatusCard(
                title = "Selesai",
                value = doneTodos.toString(),
                icon = Icons.Default.CheckCircle
            )

            // Belum
            StatusCard(
                title = "Belum",
                value = pendingTodos.toString(),
                icon = Icons.Default.Schedule
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun PreviewHomeUI() {
    DelcomTheme {
        HomeUI()
    }
}