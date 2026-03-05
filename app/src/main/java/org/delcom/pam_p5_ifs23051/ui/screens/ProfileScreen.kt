package org.delcom.pam_p5_ifs23051.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.delcom.pam_p5_ifs23051.helper.ConstHelper
import org.delcom.pam_p5_ifs23051.helper.RouteHelper
import org.delcom.pam_p5_ifs23051.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23051.ui.components.BottomDialogType
import org.delcom.pam_p5_ifs23051.ui.components.BottomDialog
import org.delcom.pam_p5_ifs23051.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthLogoutUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23051.ui.viewmodels.ProfileUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoActionUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoViewModel
import java.io.File

private const val BASE_URL = "https://pam-2026-p5-ifs23051-be.yuriii.fun:8080/"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController = rememberNavController(),
    authToken: String,
    todoViewModel: TodoViewModel,
    authViewModel: AuthViewModel,
) {
    val uiState by todoViewModel.uiState.collectAsState()
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var photoTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Sheet / dialog visibility
    var showEditSheet by remember { mutableStateOf(false) }
    var showPasswordSheet by remember { mutableStateOf(false) }
    var showAboutSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Photo picker — uses PickVisualMedia (same pattern as todo cover)
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoConfirmDialog by remember { mutableStateOf(false) }

    val profile = (uiState.profile as? ProfileUIState.Success)?.data

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            pendingPhotoUri = it
            showPhotoConfirmDialog = true
        }
    }

    // Handle photo upload result
    LaunchedEffect(uiState.profilePhoto) {
        when (val state = uiState.profilePhoto) {
            is TodoActionUIState.Success -> {
                photoTimestamp = System.currentTimeMillis()
                pendingPhotoUri = null
                snackbarHostState.showSnackbar("success|Foto profil berhasil diperbarui")
                todoViewModel.getProfile(authToken)
                todoViewModel.resetProfilePhotoState()
            }
            is TodoActionUIState.Error -> {
                snackbarHostState.showSnackbar("error|${state.message}")
                todoViewModel.resetProfilePhotoState()
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) { todoViewModel.getProfile(authToken) }

    // Handle logout result — only navigate when logout was explicitly triggered
    var logoutTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(uiStateAuth.authLogout) {
        if (!logoutTriggered) return@LaunchedEffect
        when (uiStateAuth.authLogout) {
            is AuthLogoutUIState.Success,
            is AuthLogoutUIState.Error -> {
                RouteHelper.to(
                    navController,
                    ConstHelper.RouteNames.AuthLogin.path,
                    removeBackStack = true
                )
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBarComponent(
            navController = navController,
            title = "Profile",
            showBackButton = false,
            showMenu = false,   // No menu — actions are inline buttons below
        )

        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // ── Profile photo with camera overlay (same pattern as todo cover) ──
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier
                            .size(100.dp)
                            .clickable {
                                imagePicker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                    ) {
                        // Avatar image
                        AsyncImage(
                            model = if (profile?.id != null)
                                "${BASE_URL}images/users/${profile.id}?t=$photoTimestamp"
                            else null,
                            contentDescription = "Foto Profil",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        // Camera badge (bottom-end)
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Ganti Foto",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    // Upload progress overlay
                    if (uiState.profilePhoto is TodoActionUIState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(108.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }

                // Preview new photo + Simpan button (same pattern as todo cover)
                if (pendingPhotoUri != null) {
                    Text(
                        "Foto baru dipilih",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ── User info ──────────────────────────────────────────────
                if (profile != null) {
                    Text(
                        profile.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "@${profile.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    CircularProgressIndicator()
                }

                // ── About card ─────────────────────────────────────────────
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tentang", style = MaterialTheme.typography.labelLarge)
                            IconButton(
                                onClick = { showAboutSheet = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Tentang",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = profile?.about?.takeIf { it.isNotBlank() }
                                ?: "Belum ada informasi tentang kamu.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (profile?.about.isNullOrBlank())
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider()

                // ── Action buttons ─────────────────────────────────────────
                OutlinedButton(
                    onClick = { showEditSheet = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Edit Profil (Nama & Username)") }

                OutlinedButton(
                    onClick = { showPasswordSheet = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Ubah Kata Sandi") }

                HorizontalDivider()

                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Keluar", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        BottomNavComponent(navController = navController)
    }

    // ── Photo confirm dialog (matches todo cover flow) ─────────────────────
    BottomDialog(
        show = showPhotoConfirmDialog,
        onDismiss = {
            showPhotoConfirmDialog = false
            pendingPhotoUri = null
        },
        title = "Ganti Foto Profil",
        message = "Apakah kamu yakin ingin mengganti foto profil?",
        confirmText = "Ya, Simpan",
        onConfirm = {
            pendingPhotoUri?.let { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val ext = context.contentResolver.getType(uri)
                    ?.substringAfterLast('/') ?: "jpg"
                val tempFile = File.createTempFile("photo_", ".$ext", context.cacheDir).apply {
                    outputStream().use { out -> inputStream?.copyTo(out) }
                }
                val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
                todoViewModel.updatePhoto(authToken, part)
            }
            showPhotoConfirmDialog = false
        },
        cancelText = "Batal",
        type = BottomDialogType.INFO
    )

    // ── Logout confirm dialog ──────────────────────────────────────────────
    BottomDialog(
        show = showLogoutDialog,
        onDismiss = { showLogoutDialog = false },
        title = "Keluar dari Akun",
        message = "Apakah kamu yakin ingin keluar dari akun ini?",
        confirmText = "Ya, Keluar",
        onConfirm = {
            logoutTriggered = true
            authViewModel.logout(authToken)
        },
        cancelText = "Batal",
        type = BottomDialogType.ERROR,
        destructiveAction = true
    )

    // ── Edit profile sheet ─────────────────────────────────────────────────
    if (showEditSheet) {
        EditProfileSheet(
            currentName = profile?.name ?: "",
            currentUsername = profile?.username ?: "",
            onDismiss = { showEditSheet = false },
            onSave = { name, username ->
                todoViewModel.updateProfile(authToken, name, username)
            },
            uiState = uiState.profileUpdate,
            onSuccess = {
                showEditSheet = false
                todoViewModel.getProfile(authToken)
            }
        )
    }

    // ── Change password sheet ──────────────────────────────────────────────
    if (showPasswordSheet) {
        ChangePasswordSheet(
            onDismiss = { showPasswordSheet = false },
            onSave = { oldPw, newPw -> todoViewModel.updatePassword(authToken, oldPw, newPw) },
            uiState = uiState.profilePassword,
            onSuccess = { showPasswordSheet = false }
        )
    }

    // ── Edit about sheet ───────────────────────────────────────────────────
    if (showAboutSheet) {
        EditAboutSheet(
            currentAbout = profile?.about ?: "",
            onDismiss = { showAboutSheet = false },
            onSave = { about -> todoViewModel.updateAbout(authToken, about) },
            uiState = uiState.profileAbout,
            onSuccess = {
                showAboutSheet = false
                todoViewModel.getProfile(authToken)
            }
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Bottom sheets
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(
    currentName: String,
    currentUsername: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    uiState: TodoActionUIState,
    onSuccess: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var username by remember { mutableStateOf(currentUsername) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is TodoActionUIState.Success -> onSuccess()
            is TodoActionUIState.Error -> snackbarHostState.showSnackbar(uiState.message)
            else -> {}
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Edit Profil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { if (name.isNotBlank() && username.isNotBlank()) onSave(name, username) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is TodoActionUIState.Loading
            ) {
                if (uiState is TodoActionUIState.Loading)
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordSheet(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    uiState: TodoActionUIState,
    onSuccess: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is TodoActionUIState.Success -> onSuccess()
            is TodoActionUIState.Error -> {
                snackbarHostState.showSnackbar(uiState.message)
            }
            else -> {}
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Ubah Kata Sandi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = { Text("Kata Sandi Lama") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Kata Sandi Baru") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Konfirmasi Kata Sandi Baru") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = errorText.isNotEmpty()
            )
            if (errorText.isNotEmpty()) {
                Text(
                    errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = {
                    if (newPassword != confirmPassword) {
                        errorText = "Konfirmasi kata sandi tidak cocok"
                        return@Button
                    }
                    errorText = ""
                    onSave(oldPassword, newPassword)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is TodoActionUIState.Loading
            ) {
                if (uiState is TodoActionUIState.Loading)
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAboutSheet(
    currentAbout: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    uiState: TodoActionUIState,
    onSuccess: () -> Unit
) {
    var about by remember { mutableStateOf(currentAbout) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is TodoActionUIState.Success -> onSuccess()
            is TodoActionUIState.Error -> snackbarHostState.showSnackbar(uiState.message)
            else -> {}
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Edit Tentang", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = about,
                onValueChange = { about = it },
                label = { Text("Tentang Kamu") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )
            Button(
                onClick = { onSave(about) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is TodoActionUIState.Loading
            ) {
                if (uiState is TodoActionUIState.Loading)
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        }
    }
}