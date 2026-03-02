package org.delcom.pam_p5_ifs23051.ui.screens.todos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_p5_ifs23051.R
import org.delcom.pam_p5_ifs23051.helper.ConstHelper
import org.delcom.pam_p5_ifs23051.helper.RouteHelper
import org.delcom.pam_p5_ifs23051.helper.ToolsHelper
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23051.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23051.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23051.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23051.ui.components.TopAppBarMenuItem
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthLogoutUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoViewModel
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodosUIState

@Composable
fun TodosScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel
) {
    // Ambil data dari viewmodel
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember {
        mutableStateOf(TextFieldValue(""))
    }

    // Muat data
    var todos by remember { mutableStateOf<List<ResponseTodoData>>(emptyList()) }
    var authToken by remember { mutableStateOf<String?>(null) }

    fun fetchTodosData() {
        isLoading = true

        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken

        todoViewModel.getAllTodos(authToken ?: "", searchQuery.text)
    }

    // Picu pengambilan data todos
    LaunchedEffect(Unit) {
        isLoading = true

        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(
                navController,
                ConstHelper.RouteNames.Home.path,
                true
            )
            return@LaunchedEffect
        }

        fetchTodosData()
    }

    // Picu ketika terjadi perubahan data todos
    LaunchedEffect(uiStateTodo.todos) {
        if (uiStateTodo.todos !is TodosUIState.Loading) {
            isLoading = false

            todos = if (uiStateTodo.todos is TodosUIState.Success) {
                (uiStateTodo.todos as TodosUIState.Success).data
            } else {
                emptyList()
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
    if (isLoading) {
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

    fun onOpen(todoId: String) {
        RouteHelper.to(
            navController = navController,
            destination = "todos/${todoId}"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBarComponent(
            navController = navController,
            title = "Todos",
            showBackButton = false,
            customMenuItems = menuItems,
            withSearch = true,
            searchQuery = searchQuery,
            onSearchQueryChange = { query ->
                searchQuery = query
            },
            onSearchAction = {
                fetchTodosData()
            }
        )
        // Content
        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            TodosUI(
                todos = todos,
                onOpen = ::onOpen
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
            )
            {
                // Floating Action Button
                FloatingActionButton(
                    onClick = {
                        RouteHelper.to(
                            navController,
                            ConstHelper.RouteNames
                                .TodosAdd
                                .path
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
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Tumbuhan"
                    )
                }
            }
        }
        // Bottom Nav
        BottomNavComponent(navController = navController)
    }
}

@Composable
fun TodosUI(
    todos: List<ResponseTodoData>,
    onOpen: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(todos) { todo ->
            TodoItemUI(
                todo,
                onOpen
            )
        }
    }

    if (todos.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Text(
                text = "Tidak ada data!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun TodoItemUI(
    todo: ResponseTodoData,
    onOpen: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onOpen(todo.id)
            },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            AsyncImage(
                model = ToolsHelper.getTodoImage(todo.id, todo.updatedAt),
                contentDescription = todo.title,
                placeholder = painterResource(R.drawable.img_placeholder),
                error = painterResource(R.drawable.img_placeholder),
                modifier = Modifier
                    .size(70.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = todo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (todo.isDone)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.tertiaryContainer
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (todo.isDone) "Selesai" else "Belum Selesai",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (todo.isDone)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun PreviewTodosUI() {
//    DelcomTheme {
//        TodosUI(
//            todos = DummyData.getTodosData(),
//            onOpen = {}
//        )
//    }
}