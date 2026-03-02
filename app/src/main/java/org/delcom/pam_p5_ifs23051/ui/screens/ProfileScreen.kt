package org.delcom.pam_p5_ifs23051.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_p5_ifs23051.R
import org.delcom.pam_p5_ifs23051.helper.ConstHelper
import org.delcom.pam_p5_ifs23051.helper.RouteHelper
import org.delcom.pam_p5_ifs23051.helper.ToolsHelper
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseUserData
import org.delcom.pam_p5_ifs23051.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23051.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23051.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23051.ui.components.TopAppBarMenuItem
import org.delcom.pam_p5_ifs23051.ui.theme.DelcomTheme
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthLogoutUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23051.ui.viewmodels.ProfileUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel
) {
    // Ambil data dari viewmodel
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf<ResponseUserData?>(null) }
    var authToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true

        if(uiStateAuth.auth !is AuthUIState.Success){
            RouteHelper.to(
                navController,
                ConstHelper.RouteNames.Home.path,
                true
            )
            return@LaunchedEffect
        }

        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken

        if(uiStateTodo.profile is ProfileUIState.Success){
            profile = (uiStateTodo.profile as ProfileUIState.Success).data
            isLoading = false
            return@LaunchedEffect
        }

        todoViewModel.getProfile(authToken ?: "")
    }

    LaunchedEffect(uiStateTodo.profile) {
        if(uiStateTodo.profile !is ProfileUIState.Loading){
            isLoading = false
            if(uiStateTodo.profile is ProfileUIState.Success){
                profile = (uiStateTodo.profile as ProfileUIState.Success).data
            }else{
                RouteHelper.to(
                    navController,
                    ConstHelper.RouteNames.Home.path,
                    true
                )
            }
        }
    }

    fun onLogout(token: String){
        isLoading = true
        authViewModel.logout(token)
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

    // Tampilkan halaman loading
    if(isLoading || profile == null){
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
            title = "Profile",
            showBackButton = false,
            customMenuItems = menuItems
        )

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            ProfileUI(
                profile = profile!!
            )
        }
        // Bottom Nav
        BottomNavComponent(navController = navController)
    }
}

@Composable
fun ProfileUI(
    profile: ResponseUserData
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        // Header Profile
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Foto Profil
                AsyncImage(
                    model = ToolsHelper.getUserImage(profile.id),
                    contentDescription = "Photo Profil",
                    placeholder = painterResource(R.drawable.img_placeholder),
                    error = painterResource(R.drawable.img_placeholder),
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "@${profile.username}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }


        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun PreviewProfileUI(){
    DelcomTheme {
        ProfileUI(
            profile = ResponseUserData(
                id = "",
                name = "Abdullah Ubaid",
                username = "ifs18005",
                createdAt = "",
                updatedAt = ""
            )
        )
    }
}