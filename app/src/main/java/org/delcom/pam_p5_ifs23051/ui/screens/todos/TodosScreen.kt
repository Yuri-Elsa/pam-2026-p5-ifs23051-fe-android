package org.delcom.pam_p5_ifs23051.ui.screens.todos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.delcom.pam_p5_ifs23051.helper.ConstHelper
import org.delcom.pam_p5_ifs23051.helper.RouteHelper
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23051.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23051.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoActionUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoViewModel
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodosUIState

private const val PER_PAGE = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodosScreen(
    navController: NavHostController = rememberNavController(),
    authToken: String,
    todoViewModel: TodoViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
) {
    // Auth guard
    if (authToken.isBlank()) {
        LaunchedEffect(Unit) {
            RouteHelper.to(
                navController,
                ConstHelper.RouteNames.AuthLogin.path,
                removeBackStack = true
            )
        }
        return
    }

    val uiState by todoViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    var selectedIsDone by remember { mutableStateOf<Boolean?>(null) }
    var selectedUrgency by remember { mutableStateOf<String?>(null) }

    // ✅ FIX: Pisahkan nextPageToLoad dari halaman yang sudah berhasil di-load
    // Sebelumnya currentPage++ di LaunchedEffect(uiState.todos) bisa race condition
    // → halaman sama di-fetch 2x → duplicate todo ID → crash LazyColumn
    var nextPageToLoad by remember { mutableStateOf(1) }
    var allTodos by remember { mutableStateOf<List<ResponseTodoData>>(emptyList()) }
    var hasNextPage by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var isFirstLoad by remember { mutableStateOf(true) }

    // ✅ FIX: Set untuk mencegah duplicate item masuk ke list
    val loadedIds = remember { mutableSetOf<String>() }

    val listState = rememberLazyListState()

    fun loadPage(page: Int, isDone: Boolean?, urgency: String?) {
        todoViewModel.getAllTodos(
            authToken = authToken,
            page = page,
            perPage = PER_PAGE,
            isDone = isDone,
            urgency = urgency
        )
    }

    fun resetAndLoad(isDone: Boolean? = selectedIsDone, urgency: String? = selectedUrgency) {
        nextPageToLoad = 1
        allTodos = emptyList()
        loadedIds.clear()
        hasNextPage = false
        isLoadingMore = false
        isFirstLoad = true
        loadPage(1, isDone, urgency)
    }

    LaunchedEffect(authToken) { resetAndLoad() }

    // Reload saat kembali dari AddScreen
    LaunchedEffect(navBackStackEntry) {
        val savedStateHandle = navBackStackEntry?.savedStateHandle
        val todoAdded = savedStateHandle?.get<Boolean>("todo_added") ?: false
        if (todoAdded) {
            savedStateHandle?.remove<Boolean>("todo_added")
            resetAndLoad()
        }
    }

    // Reload setelah delete
    LaunchedEffect(uiState.todoDelete) {
        if (uiState.todoDelete is TodoActionUIState.Success) {
            todoViewModel.resetTodoDeleteState()
            resetAndLoad()
        }
    }

    LaunchedEffect(uiState.todos) {
        when (val state = uiState.todos) {
            is TodosUIState.Success -> {
                // ✅ FIX: Filter item duplikat sebelum ditambahkan ke list
                val newItems = state.data.filter { it.id !in loadedIds }
                loadedIds.addAll(newItems.map { it.id })

                if (isFirstLoad) {
                    allTodos = newItems
                    isFirstLoad = false
                } else {
                    allTodos = allTodos + newItems
                }

                // ✅ FIX: Update nextPageToLoad berdasarkan response server
                nextPageToLoad = state.pagination.currentPage + 1
                hasNextPage = state.pagination.hasNextPage
                isLoadingMore = false
            }
            is TodosUIState.Error -> {
                isLoadingMore = false
                isFirstLoad = false
            }
            is TodosUIState.Loading -> { /* ditangani oleh isFirstLoad & isLoadingMore */ }
        }
    }

    // ✅ FIX: Trigger infinite scroll yang lebih aman dengan .filter { it }
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = layoutInfo.totalItemsCount
                totalItems > 0 && lastVisible >= totalItems - 3
            }
            .distinctUntilChanged()
            .filter { it } // Hanya emit saat near end = true
            .collect {
                if (!isLoadingMore && hasNextPage) {
                    isLoadingMore = true
                    loadPage(nextPageToLoad, selectedIsDone, selectedUrgency)
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBarComponent(
            navController = navController,
            title = "Todos",
            showBackButton = false,
        )

        Box(modifier = Modifier.weight(1f)) {
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(onClick = onNavigateToAdd) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Todo")
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    TodoFilterRow(
                        selectedIsDone = selectedIsDone,
                        selectedUrgency = selectedUrgency,
                        onIsDoneChanged = { newValue ->
                            selectedIsDone = newValue
                            resetAndLoad(isDone = newValue)
                        },
                        onUrgencyChanged = { newValue ->
                            selectedUrgency = newValue
                            resetAndLoad(urgency = newValue)
                        }
                    )

                    when {
                        isFirstLoad && uiState.todos is TodosUIState.Loading -> {
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        allTodos.isEmpty() -> {
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Tidak ada todo.")
                            }
                        }
                        else -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // ✅ FIX: Key dijamin unik karena loadedIds filter duplikat di atas
                                itemsIndexed(
                                    items = allTodos,
                                    key = { _, todo -> todo.id }
                                ) { _, todo ->
                                    TodoItemUI(
                                        todo = todo,
                                        onClick = { onNavigateToDetail(todo.id) },
                                        onDelete = { todoViewModel.deleteTodo(authToken, todo.id) }
                                    )
                                }

                                if (isLoadingMore) {
                                    item(key = "loading_more") {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        BottomNavComponent(navController = navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoFilterRow(
    selectedIsDone: Boolean?,
    selectedUrgency: String?,
    onIsDoneChanged: (Boolean?) -> Unit,
    onUrgencyChanged: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = selectedIsDone == null,
            onClick = { onIsDoneChanged(null) },
            label = { Text("Semua") }
        )
        FilterChip(
            selected = selectedIsDone == false,
            onClick = { onIsDoneChanged(false) },
            label = { Text("Belum Selesai") }
        )
        FilterChip(
            selected = selectedIsDone == true,
            onClick = { onIsDoneChanged(true) },
            label = { Text("Selesai") }
        )
        VerticalDivider(modifier = Modifier.height(28.dp))
        FilterChip(
            selected = selectedUrgency == null,
            onClick = { onUrgencyChanged(null) },
            label = { Text("Semua Urgensi") }
        )
        FilterChip(
            selected = selectedUrgency == "low",
            onClick = { onUrgencyChanged(if (selectedUrgency == "low") null else "low") },
            label = { Text("Rendah") }
        )
        FilterChip(
            selected = selectedUrgency == "medium",
            onClick = { onUrgencyChanged(if (selectedUrgency == "medium") null else "medium") },
            label = { Text("Sedang") }
        )
        FilterChip(
            selected = selectedUrgency == "high",
            onClick = { onUrgencyChanged(if (selectedUrgency == "high") null else "high") },
            label = { Text("Tinggi") }
        )
    }
}