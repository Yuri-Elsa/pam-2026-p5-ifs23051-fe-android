package org.delcom.pam_p5_ifs23051.ui.screens.todos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.delcom.pam_p5_ifs23051.helper.AlertHelper
import org.delcom.pam_p5_ifs23051.helper.AlertState
import org.delcom.pam_p5_ifs23051.helper.AlertType
import org.delcom.pam_p5_ifs23051.helper.ConstHelper
import org.delcom.pam_p5_ifs23051.helper.RouteHelper
import org.delcom.pam_p5_ifs23051.helper.SuspendHelper
import org.delcom.pam_p5_ifs23051.helper.SuspendHelper.SnackBarType
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23051.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23051.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23051.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoActionUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoViewModel

@Composable
fun TodosEditScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel,
    todoId: String
) {
    // Ambil data dari viewmodel
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }

    // Muat data
    var todo by remember { mutableStateOf<ResponseTodoData?>(null) }
    val authToken = remember { mutableStateOf<String?>(null) }

    // Dapatkan tumbuhan berdasarkan ID
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

        authToken.value = (uiStateAuth.auth as AuthUIState.Success).data.authToken

        uiStateTodo.todo = TodoUIState.Loading
        uiStateTodo.todoChange = TodoActionUIState.Loading

        // Reset status todo action
        todoViewModel.getTodoById(authToken.value!!, todoId)
    }

    // Picu ulang ketika data tumbuhan berubah
    LaunchedEffect(uiStateTodo.todo) {
        if (uiStateTodo.todo !is TodoUIState.Loading) {
            if (uiStateTodo.todo is TodoUIState.Success) {
                todo = (uiStateTodo.todo as TodoUIState.Success).data
                isLoading = false
            } else {
                RouteHelper.back(navController)
                isLoading = false
            }
        }
    }

    // Simpan perubahan data
    fun onSave(
        title: String,
        description: String,
        isDone: Boolean,
    ) {
        isLoading = true

        todoViewModel.putTodo(
            authToken = authToken.value!!,
            todoId = todoId,
            title = title,
            description = description,
            isDone = isDone
        )
    }

    LaunchedEffect(uiStateTodo.todoChange) {
        when (val state = uiStateTodo.todoChange) {
            is TodoActionUIState.Success -> {
                SuspendHelper.showSnackBar(
                    snackbarHost = snackbarHost,
                    type = SnackBarType.SUCCESS,
                    message = state.message
                )
                RouteHelper.to(
                    navController = navController,
                    destination = ConstHelper.RouteNames.TodosDetail.path
                        .replace("{todoId}", todoId),
                    popUpTo = ConstHelper.RouteNames.TodosDetail.path
                        .replace("{todoId}", todoId),
                    removeBackStack = true
                )
                isLoading = false
            }

            is TodoActionUIState.Error -> {
                SuspendHelper.showSnackBar(
                    snackbarHost = snackbarHost,
                    type = SnackBarType.ERROR,
                    message = state.message
                )
                isLoading = false
            }

            else -> {}
        }
    }

    // Tampilkan halaman loading
    if (isLoading || todo == null) {
        LoadingUI()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBarComponent(
            navController = navController,
            title = "Ubah Data",
            showBackButton = true,
        )
        // Content
        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            TodosEditUI(
                todo = todo!!,
                onSave = ::onSave
            )
        }
        // Bottom Nav
        BottomNavComponent(navController = navController)
    }
}

@Composable
fun TodosEditUI(
    todo: ResponseTodoData,
    onSave: (
        String,
        String,
        Boolean,
    ) -> Unit
) {
    val alertState = remember { mutableStateOf(AlertState()) }

    var dataTitle by remember { mutableStateOf(todo.title) }
    var dataDescription by remember { mutableStateOf(todo.description) }
    var dataIsDone by remember { mutableStateOf(todo.isDone) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Nama
        OutlinedTextField(
            value = dataTitle,
            onValueChange = { dataTitle = it },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                cursorColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            label = {
                Text(
                    text = "Title",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
        )

        // Is Done
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Is Done?",
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = dataIsDone,
                    onClick = { dataIsDone = true }
                )
                Text("Yes")

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = !dataIsDone,
                    onClick = { dataIsDone = false }
                )
                Text("No")
            }
        }

        // Deskripsi
        OutlinedTextField(
            value = dataDescription,
            onValueChange = { dataDescription = it },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                cursorColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            label = {
                Text(
                    text = "Deskripsi",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            maxLines = 5,
            minLines = 3
        )

        Spacer(modifier = Modifier.height(64.dp))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Floating Action Button
        FloatingActionButton(
            onClick = {
                if (dataTitle.isEmpty()) {
                    AlertHelper.show(
                        alertState,
                        AlertType.ERROR,
                        "Judul tidak boleh kosong!"
                    )
                    return@FloatingActionButton
                }

                if (dataDescription.isEmpty()) {
                    AlertHelper.show(
                        alertState,
                        AlertType.ERROR,
                        "Deskripsi tidak boleh kosong!"
                    )
                    return@FloatingActionButton
                }

                onSave(
                    dataTitle,
                    dataDescription,
                    dataIsDone
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd) // pojok kanan bawah
                .padding(16.dp) // jarak dari tepi
            ,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Simpan Data"
            )
        }
    }

    if (alertState.value.isVisible) {
        AlertDialog(
            onDismissRequest = {
                AlertHelper.dismiss(alertState)
            },
            title = {
                Text(alertState.value.type.title)
            },
            text = {
                Text(alertState.value.message)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        AlertHelper.dismiss(alertState)
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun PreviewTodosEditUI() {
//    DelcomTheme {
//        TodosEditUI(
//            todos = DummyData.getTodosEditData(),
//            onOpen = {}
//        )
//    }
}