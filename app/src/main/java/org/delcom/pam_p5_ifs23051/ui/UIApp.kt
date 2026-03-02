package org.delcom.pam_p5_ifs23051.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.delcom.pam_p5_ifs23051.helper.ConstHelper
import org.delcom.pam_p5_ifs23051.ui.components.CustomSnackbar
import org.delcom.pam_p5_ifs23051.ui.screens.HomeScreen
import org.delcom.pam_p5_ifs23051.ui.screens.ProfileScreen
import org.delcom.pam_p5_ifs23051.ui.screens.auth.AuthLoginScreen
import org.delcom.pam_p5_ifs23051.ui.screens.auth.AuthRegisterScreen
import org.delcom.pam_p5_ifs23051.ui.screens.todos.TodosAddScreen
import org.delcom.pam_p5_ifs23051.ui.screens.todos.TodosDetailScreen
import org.delcom.pam_p5_ifs23051.ui.screens.todos.TodosEditScreen
import org.delcom.pam_p5_ifs23051.ui.screens.todos.TodosScreen
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UIApp(
    navController: NavHostController = rememberNavController(),
    todoViewModel: TodoViewModel,
    authViewModel: AuthViewModel
) {
    // Inisialisasi SnackbarHostState
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState){ snackbarData ->
            CustomSnackbar(snackbarData, onDismiss = { snackbarHostState.currentSnackbarData?.dismiss() })
        } },
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = ConstHelper.RouteNames.Home.path,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FA))

        ) {
            // Auth Login
            composable(
                route = ConstHelper.RouteNames.AuthLogin.path,
            ) { _ ->
                AuthLoginScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                )
            }

            // Auth Register
            composable(
                route = ConstHelper.RouteNames.AuthRegister.path,
            ) { _ ->
                AuthRegisterScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                )
            }

            // Home
            composable(
                route = ConstHelper.RouteNames.Home.path,
            ) { _ ->
                HomeScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    todoViewModel = todoViewModel
                )
            }

            // Profile
            composable(
                route = ConstHelper.RouteNames.Profile.path,
            ) { _ ->
                ProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    todoViewModel = todoViewModel
                )
            }

            // Todos
            composable(
                route = ConstHelper.RouteNames.Todos.path,
            ) { _ ->
                TodosScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    todoViewModel = todoViewModel
                )
            }

            // Todos Add
            composable(
                route = ConstHelper.RouteNames.TodosAdd.path,
            ) { _ ->
                TodosAddScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                    todoViewModel = todoViewModel
                )
            }

            // Todos Detail
            composable(
                route = ConstHelper.RouteNames.TodosDetail.path,
                arguments = listOf(
                    navArgument("todoId") { type = NavType.StringType },
                )
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

            // Todos Edit
            composable(
                route = ConstHelper.RouteNames.TodosEdit.path,
                arguments = listOf(
                    navArgument("todoId") { type = NavType.StringType },
                )
            ) { backStackEntry ->
                val todoId = backStackEntry.arguments?.getString("todoId") ?: ""

                TodosEditScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                    todoViewModel = todoViewModel,
                    todoId = todoId
                )
            }
        }
    }

}