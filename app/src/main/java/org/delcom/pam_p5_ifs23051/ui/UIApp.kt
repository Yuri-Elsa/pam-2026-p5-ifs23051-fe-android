package org.delcom.pam_p5_ifs23051.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.delcom.pam_p5_ifs23051.helper.ConstHelper
import org.delcom.pam_p5_ifs23051.helper.RouteHelper
import org.delcom.pam_p5_ifs23051.ui.components.CustomSnackbar
import org.delcom.pam_p5_ifs23051.ui.screens.home.HomeScreen
import org.delcom.pam_p5_ifs23051.ui.screens.profile.ProfileScreen
import org.delcom.pam_p5_ifs23051.ui.screens.auth.AuthLoginScreen
import org.delcom.pam_p5_ifs23051.ui.screens.auth.AuthRegisterScreen
import org.delcom.pam_p5_ifs23051.ui.screens.todos.TodosAddScreen
import org.delcom.pam_p5_ifs23051.ui.screens.todos.TodosDetailScreen
import org.delcom.pam_p5_ifs23051.ui.screens.todos.TodosEditScreen
import org.delcom.pam_p5_ifs23051.ui.screens.todos.TodosScreen
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UIApp(
    navController: NavHostController = rememberNavController(),
    todoViewModel: TodoViewModel,
    authViewModel: AuthViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiStateAuth by authViewModel.uiState.collectAsState()

    val authState = uiStateAuth.auth
    val authToken = (authState as? AuthUIState.Success)?.data?.authToken ?: ""

    LaunchedEffect(authState) {
        when (authState) {
            is AuthUIState.Error -> {
                RouteHelper.to(
                    navController,
                    ConstHelper.RouteNames.AuthLogin.path,
                    removeBackStack = true
                )
            }
            is AuthUIState.Success -> {
                val currentRoute = navController.currentDestination?.route
                val isOnAuthPage = currentRoute == ConstHelper.RouteNames.AuthLogin.path ||
                        currentRoute == ConstHelper.RouteNames.AuthRegister.path
                if (isOnAuthPage || currentRoute == null) {
                    RouteHelper.to(
                        navController,
                        ConstHelper.RouteNames.Home.path,
                        removeBackStack = true
                    )
                }
            }
            is AuthUIState.Loading -> {}
        }
    }

    if (authState is AuthUIState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FA)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { snackbarData ->
                CustomSnackbar(
                    snackbarData,
                    onDismiss = { snackbarHostState.currentSnackbarData?.dismiss() }
                )
            }
        },
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = ConstHelper.RouteNames.Home.path,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FA))
        ) {
            composable(route = ConstHelper.RouteNames.AuthLogin.path) {
                AuthLoginScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                )
            }

            composable(route = ConstHelper.RouteNames.AuthRegister.path) {
                AuthRegisterScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                )
            }

            composable(route = ConstHelper.RouteNames.Home.path) {
                HomeScreen(
                    navController = navController,
                    authToken = authToken,
                    todoViewModel = todoViewModel,
                )
            }

            composable(route = ConstHelper.RouteNames.Profile.path) {
                ProfileScreen(
                    navController = navController,
                    authToken = authToken,
                    todoViewModel = todoViewModel,
                    authViewModel = authViewModel,
                )
            }

            composable(route = ConstHelper.RouteNames.Todos.path) {
                TodosScreen(
                    navController = navController,
                    authToken = authToken,
                    todoViewModel = todoViewModel,
                    onNavigateToAdd = {
                        RouteHelper.to(navController, ConstHelper.RouteNames.TodosAdd.path)
                    },
                    onNavigateToDetail = { todoId ->
                        RouteHelper.to(
                            navController,
                            ConstHelper.RouteNames.TodosDetail.path.replace("{todoId}", todoId)
                        )
                    }
                )
            }

            composable(route = ConstHelper.RouteNames.TodosAdd.path) {
                // Pass navController agar bisa set savedStateHandle ke TodosScreen
                TodosAddScreen(
                    authToken = authToken,
                    todoViewModel = todoViewModel,
                    navController = navController,
                    onNavigateBack = { RouteHelper.back(navController) }
                )
            }

            composable(
                route = ConstHelper.RouteNames.TodosDetail.path,
                arguments = listOf(navArgument("todoId") { type = NavType.StringType })
            ) { backStackEntry ->
                val todoId = backStackEntry.arguments?.getString("todoId") ?: ""
                TodosDetailScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                    todoViewModel = todoViewModel,
                    todoId = todoId
                )
            }

            composable(
                route = ConstHelper.RouteNames.TodosEdit.path,
                arguments = listOf(navArgument("todoId") { type = NavType.StringType })
            ) { backStackEntry ->
                val todoId = backStackEntry.arguments?.getString("todoId") ?: ""
                TodosEditScreen(
                    authToken = authToken,
                    todoId = todoId,
                    todoViewModel = todoViewModel,
                    onNavigateBack = { RouteHelper.back(navController) }
                )
            }
        }
    }
}