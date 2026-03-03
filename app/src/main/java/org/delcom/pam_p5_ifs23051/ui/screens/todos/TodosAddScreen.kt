package org.delcom.pam_p5_ifs23051.ui.screens.todos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.delcom.pam_p5_ifs23051.ui.components.UrgencySelector
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoActionUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodosAddScreen(
    authToken: String,
    todoViewModel: TodoViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by todoViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf("medium") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.todoAdd) {
        when (val state = uiState.todoAdd) {
            is TodoActionUIState.Success -> {
                isLoading = false
                snackbarHostState.showSnackbar("Todo berhasil ditambahkan")
                onNavigateBack()
            }
            is TodoActionUIState.Error -> {
                isLoading = false
                snackbarHostState.showSnackbar(state.message)
            }
            is TodoActionUIState.Loading -> { /* handled by isLoading */ }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tambah Todo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Judul") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Urgency selector
            Text("Urgensi", style = MaterialTheme.typography.labelLarge)
            UrgencySelector(
                selectedUrgency = urgency,
                onUrgencySelected = { urgency = it }
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank()) return@Button
                    isLoading = true
                    todoViewModel.postTodo(authToken, title, description, urgency)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Simpan")
                }
            }
        }
    }
}
