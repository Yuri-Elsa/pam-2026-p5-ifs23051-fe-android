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
import kotlinx.coroutines.flow.map
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
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
    // Guard: jangan render apapun kalau token belum siap
    if (authToken.isBlank()) return

    val uiState by todoViewModel.uiState.collectAsState()

    var selectedIsDone by remember { mutableStateOf<Boolean?>(null) }
    var selectedUrgency by remember { mutableStateOf<String?>(null) }

    // Gunakan satu sumber kebenaran untuk semua pagination state
    var currentPage by remember { mutableStateOf(1) }
    var allTodos by remember { mutableStateOf<List<ResponseTodoData>>(emptyList()) }
    var hasNextPage by remember { mutableStateOf(false) }
    // isLoadingMore diproteksi agar tidak bisa false sebelum data benar-benar tiba
    var isLoadingMore by remember { mutableStateOf(false) }
    // Flag untuk membedakan load pertama vs load lebih
    var isFirstLoad by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()

    fun loadFirstPage() {
        currentPage = 1
        allTodos = emptyList()
        hasNextPage = false
        isLoadingMore = false
        isFirstLoad = true
        todoViewModel.getAllTodos(
            authToken = authToken,
            page = 1,
            perPage = PER_PAGE,
            isDone = selectedIsDone,
            urgency = selectedUrgency
        )
    }

    fun loadNextPage() {
        if (!hasNextPage || isLoadingMore) return
        isLoadingMore = true
        todoViewModel.getAllTodos(
            authToken = authToken,
            page = currentPage + 1,
            perPage = PER_PAGE,
            isDone = selectedIsDone,
            urgency = selectedUrgency
        )
    }

    // Load pertama kali — tunggu token tersedia
    LaunchedEffect(authToken) {
        loadFirstPage()
    }

    // Proses perubahan state todos
    LaunchedEffect(uiState.todos) {
        when (val state = uiState.todos) {
            is TodosUIState.Success -> {
                if (isFirstLoad) {
                    // Halaman pertama: ganti seluruh list
                    allTodos = state.data
                    isFirstLoad = false
                } else {
                    // Halaman berikutnya: append
                    allTodos = allTodos + state.data
                    currentPage++
                }
                hasNextPage = state.pagination.hasNextPage
                isLoadingMore = false
            }
            is TodosUIState.Error -> {
                isLoadingMore = false
                isFirstLoad = false
            }
            is TodosUIState.Loading -> { /* ditangani oleh flag isFirstLoad */ }
        }
    }

    // Reload setelah delete berhasil
    LaunchedEffect(uiState.todoDelete) {
        if (uiState.todoDelete is TodoActionUIState.Success) {
            loadFirstPage()
        }
    }

    // Infinite scroll trigger
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = layoutInfo.totalItemsCount
                totalItems > 0 && lastVisible >= totalItems - 3
            }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd && !isLoadingMore && hasNextPage) {
                    loadNextPage()
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
                            loadFirstPage()
                        },
                        onUrgencyChanged = { newValue ->
                            selectedUrgency = newValue
                            loadFirstPage()
                        }
                    )

                    when {
                        // Tampilkan loading spinner hanya saat load pertama & list masih kosong
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
                                itemsIndexed(
                                    items = allTodos,
                                    key = { _, todo -> todo.id }
                                ) { _, todo ->
                                    TodoItemUI(
                                        todo = todo,
                                        onClick = { onNavigateToDetail(todo.id) },
                                        onDelete = {
                                            todoViewModel.deleteTodo(authToken, todo.id)
                                        }
                                    )
                                }
                                if (isLoadingMore) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
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