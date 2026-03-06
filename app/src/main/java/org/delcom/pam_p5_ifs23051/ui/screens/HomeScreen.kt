package org.delcom.pam_p5_ifs23051.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.delcom.pam_p5_ifs23051.helper.ConstHelper
import org.delcom.pam_p5_ifs23051.helper.RouteHelper
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23051.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23051.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23051.ui.screens.todos.TodoItemUI
import org.delcom.pam_p5_ifs23051.ui.viewmodels.HomeTodosUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.StatsUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoActionUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoViewModel

private const val PER_PAGE = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController = rememberNavController(),
    authToken: String,
    todoViewModel: TodoViewModel,
) {
    val isAuthenticated = authToken.isNotBlank()

    val uiState by todoViewModel.uiState.collectAsState()
    val homeTodosState by todoViewModel.homeTodosState.collectAsState()

    // ── Stats ──────────────────────────────────────────────────────────────
    LaunchedEffect(authToken) {
        if (isAuthenticated) todoViewModel.getStats(authToken)
    }

    val stats = (uiState.stats as? StatsUIState.Success)?.data
    val isStatsLoading = uiState.stats is StatsUIState.Loading

    // ── Infinite scroll state ──────────────────────────────────────────────
    // ✅ FIX: Pisahkan "nextPage yang akan di-fetch" dari "currentPage yang sudah di-fetch"
    // Sebelumnya currentPage++ dilakukan di dalam LaunchedEffect(homeTodosState) yang
    // bisa dipanggil ulang akibat recomposition → halaman yang sama di-fetch 2x → duplicate key crash
    var nextPageToLoad by remember { mutableStateOf(1) }
    var allTodos by remember { mutableStateOf<List<ResponseTodoData>>(emptyList()) }
    var hasNextPage by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var isFirstLoad by remember { mutableStateOf(true) }

    // ✅ FIX: Gunakan Set untuk melacak ID yang sudah ada, cegah duplicate
    val loadedIds = remember { mutableSetOf<String>() }

    val listState = rememberLazyListState()

    fun loadPage(page: Int) {
        if (isAuthenticated) {
            todoViewModel.getHomeTodos(authToken = authToken, page = page, perPage = PER_PAGE)
        }
    }

    fun resetAndLoad() {
        nextPageToLoad = 1
        allTodos = emptyList()
        loadedIds.clear()
        hasNextPage = false
        isLoadingMore = false
        isFirstLoad = true
        loadPage(1)
    }

    LaunchedEffect(authToken) {
        if (isAuthenticated) resetAndLoad()
    }

    // Reload after delete
    LaunchedEffect(uiState.todoDelete) {
        if (uiState.todoDelete is TodoActionUIState.Success) {
            todoViewModel.resetTodoDeleteState()
            if (isAuthenticated) {
                resetAndLoad()
                todoViewModel.getStats(authToken)
            }
        }
    }

    LaunchedEffect(homeTodosState) {
        when (val state = homeTodosState) {
            is HomeTodosUIState.Success -> {
                // ✅ FIX: Filter item yang sudah ada sebelum menambahkan ke list
                // Ini mencegah IllegalArgumentException "Key was already used" di LazyColumn
                val newItems = state.data.filter { it.id !in loadedIds }
                loadedIds.addAll(newItems.map { it.id })

                if (isFirstLoad) {
                    allTodos = newItems
                    isFirstLoad = false
                } else {
                    allTodos = allTodos + newItems
                }

                // ✅ FIX: Increment nextPageToLoad di sini (bukan di trigger scroll)
                // agar tidak ada race condition antara "sudah fetch" vs "belum fetch"
                if (newItems.isNotEmpty() || state.pagination.hasNextPage) {
                    nextPageToLoad = state.pagination.currentPage + 1
                }

                hasNextPage = state.pagination.hasNextPage
                isLoadingMore = false
            }
            is HomeTodosUIState.Error -> {
                isLoadingMore = false
                isFirstLoad = false
            }
            is HomeTodosUIState.Loading -> { /* ditangani oleh isFirstLoad & isLoadingMore */ }
        }
    }

    // ✅ FIX: Infinite scroll trigger yang aman
    // Gunakan filter untuk memastikan hanya trigger saat benar-benar near end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = layoutInfo.totalItemsCount
                totalItems > 0 && lastVisible >= totalItems - 3
            }
            .distinctUntilChanged()
            .filter { it } // ✅ Hanya emit saat true (near end)
            .collect {
                // ✅ FIX: Cek isLoadingMore dan hasNextPage sebelum fetch
                if (!isLoadingMore && hasNextPage && isAuthenticated) {
                    isLoadingMore = true
                    loadPage(nextPageToLoad)
                }
            }
    }

    // ── UI ─────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBarComponent(
            navController = navController,
            title = "Home",
            showBackButton = false,
        )

        Box(modifier = Modifier.weight(1f)) {
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            if (isAuthenticated) {
                                RouteHelper.to(navController, ConstHelper.RouteNames.TodosAdd.path)
                            } else {
                                RouteHelper.to(
                                    navController,
                                    ConstHelper.RouteNames.AuthLogin.path,
                                    removeBackStack = true
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Todo")
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { paddingValues ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // ── Banner ─────────────────────────────────────────────
                    item(key = "banner") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.List,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "My Todos",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    // ── Not logged in banner ───────────────────────────────
                    if (!isAuthenticated) {
                        item(key = "not_auth_banner") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Masuk untuk melihat dan mengelola todos kamu",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Button(
                                        onClick = {
                                            RouteHelper.to(
                                                navController,
                                                ConstHelper.RouteNames.AuthLogin.path,
                                                removeBackStack = true
                                            )
                                        }
                                    ) {
                                        Text("Masuk / Daftar")
                                    }
                                }
                            }
                        }
                        return@LazyColumn
                    }

                    // ── Stats header ───────────────────────────────────────
                    item(key = "stats_header") {
                        Text(
                            text = "Statistik Todo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    item(key = "stats_row") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = "Total",
                                value = stats?.total?.toString() ?: "-",
                                icon = Icons.Filled.List,
                                iconTint = MaterialTheme.colorScheme.primary,
                                isLoading = isStatsLoading
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = "Selesai",
                                value = stats?.done?.toString() ?: "-",
                                icon = Icons.Filled.CheckCircle,
                                iconTint = Color(0xFF13DEB9),
                                isLoading = isStatsLoading
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = "Belum",
                                value = stats?.pending?.toString() ?: "-",
                                icon = Icons.Filled.PendingActions,
                                iconTint = Color(0xFFFA896B),
                                isLoading = isStatsLoading
                            )
                        }
                    }

                    // ── Todos header ───────────────────────────────────────
                    item(key = "todos_header") {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Daftar Todo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    // ── Todos list ─────────────────────────────────────────
                    if (isFirstLoad && homeTodosState is HomeTodosUIState.Loading) {
                        item(key = "loading_initial") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        }
                    } else if (allTodos.isEmpty()) {
                        item(key = "empty_state") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Belum ada todo. Tambahkan sekarang!",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // ✅ FIX: key = { _, todo -> todo.id } sudah benar,
                        // tapi sekarang dijamin unik karena loadedIds filter duplikat di atas
                        itemsIndexed(
                            items = allTodos,
                            key = { _, todo -> todo.id }
                        ) { _, todo ->
                            TodoItemUI(
                                todo = todo,
                                onClick = {
                                    RouteHelper.to(
                                        navController,
                                        ConstHelper.RouteNames.TodosDetail.path
                                            .replace("{todoId}", todo.id)
                                    )
                                },
                                onDelete = { todoViewModel.deleteTodo(authToken, todo.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
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
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
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

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = iconTint
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}