package org.delcom.pam_p5_ifs23051.ui.screens.todos

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
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoActionUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoViewModel
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodosUIState

private const val PER_PAGE = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodosScreen(
    authToken: String,
    todoViewModel: TodoViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
) {
    val uiState by todoViewModel.uiState.collectAsState()

    // ── Filter state ──────────────────────────────────────
    var selectedIsDone by remember { mutableStateOf<Boolean?>(null) }
    var selectedUrgency by remember { mutableStateOf<String?>(null) }

    // ── Pagination state ──────────────────────────────────
    var currentPage by remember { mutableStateOf(1) }
    var allTodos by remember { mutableStateOf<List<ResponseTodoData>>(emptyList()) }
    var hasNextPage by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Helper: load halaman pertama (reset list)
    fun loadFirstPage() {
        currentPage = 1
        allTodos = emptyList()
        todoViewModel.getAllTodos(
            authToken = authToken,
            page = 1,
            perPage = PER_PAGE,
            isDone = selectedIsDone,
            urgency = selectedUrgency
        )
    }

    // Helper: load halaman berikutnya (append)
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

    // Load pertama kali
    LaunchedEffect(Unit) { loadFirstPage() }

    // Handle hasil API
    LaunchedEffect(uiState.todos) {
        when (val state = uiState.todos) {
            is TodosUIState.Success -> {
                if (currentPage == 1) {
                    allTodos = state.data
                } else {
                    allTodos = allTodos + state.data
                }
                hasNextPage = state.pagination.hasNextPage
                if (isLoadingMore) {
                    currentPage++
                    isLoadingMore = false
                }
            }
            is TodosUIState.Error -> { isLoadingMore = false }
            else -> {}
        }
    }

    // Handle hapus todo — refresh list setelah berhasil
    LaunchedEffect(uiState.todoDelete) {
        if (uiState.todoDelete is TodoActionUIState.Success) {
            loadFirstPage()
        }
    }

    // Infinite scroll: deteksi scroll mendekati akhir list
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = layoutInfo.totalItemsCount
                lastVisible >= totalItems - 3
            }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd && !isLoadingMore && hasNextPage) {
                    loadNextPage()
                }
            }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Todo")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Filter Row ────────────────────────────────────
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

            // ── Daftar Todo ───────────────────────────────────
            when {
                uiState.todos is TodosUIState.Loading && allTodos.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                allTodos.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                        itemsIndexed(allTodos) { _, todo ->
                            TodoItemUI(
                                todo = todo,
                                onClick = { onNavigateToDetail(todo.id) },
                                onDelete = { todoViewModel.deleteTodo(authToken, todo.id) }
                            )
                        }
                        // Loading more indicator
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoFilterRow(
    selectedIsDone: Boolean?,
    selectedUrgency: String?,
    onIsDoneChanged: (Boolean?) -> Unit,
    onUrgencyChanged: (String?) -> Unit
) {
    val scrollState = androidx.compose.foundation.rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Filter status
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

        // Separator
        VerticalDivider(modifier = Modifier.height(28.dp))

        // Filter urgency
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
