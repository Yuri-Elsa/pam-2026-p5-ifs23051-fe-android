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
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodosEditScreen(
    authToken: String,
    todoId: String,
    todoViewModel: TodoViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by todoViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isDone by remember { mutableStateOf(false) }
    var urgency by remember { mutableStateOf("medium") }
    var isLoading by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }

    // Load data todo
    LaunchedEffect(todoId) {
        todoViewModel.getTodoById(authToken, todoId)
    }

    // Isi form saat data berhasil dimuat
    LaunchedEffect(uiState.todo) {
        if (!isInitialized && uiState.todo is TodoUIState.Success) {
            val todo = (uiState.todo as TodoUIState.Success).data
            title = todo.title
            description = todo.description
            isDone = todo.isDone
            urgency = todo.urgency
            isInitialized = true
        }
    }

    // Handle hasil update
    LaunchedEffect(uiState.todoChange) {
        when (val state = uiState.todoChange) {
            is TodoActionUIState.Success -> {
                isLoading = false
                snackbarHostState.showSnackbar("Todo berhasil diperbarui")
                onNavigateBack()
            }
            is TodoActionUIState.Error -> {
                isLoading = false
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Todo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            !isInitialized && uiState.todo is TodoUIState.Loading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {
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

                    // Status selesai
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text("Tandai Selesai")
                        Switch(checked = isDone, onCheckedChange = { isDone = it })
                    }

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
                            todoViewModel.putTodo(authToken, todoId, title, description, isDone, urgency)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Simpan Perubahan")
                        }
                    }
                }
            }
        }
    }
}
