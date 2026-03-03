package org.delcom.pam_p5_ifs23051.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.delcom.pam_p5_ifs23051.ui.viewmodels.StatsUIState
import org.delcom.pam_p5_ifs23051.ui.viewmodels.TodoViewModel

@Composable
fun HomeScreen(
    authToken: String,
    todoViewModel: TodoViewModel,
    // parameter lain yang sudah ada sebelumnya (navController, dsb.)
) {
    val uiState by todoViewModel.uiState.collectAsState()

    // Ambil statistik saat screen pertama kali ditampilkan
    LaunchedEffect(Unit) {
        todoViewModel.getStats(authToken)
    }

    val stats = when (val s = uiState.stats) {
        is StatsUIState.Success -> s.data
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Header / sambutan ──────────────────────────
        Text("Beranda", style = MaterialTheme.typography.headlineSmall)

        // ── Kartu Statistik ───────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Total",
                value = stats?.total?.toString() ?: "-",
                isLoading = uiState.stats is StatsUIState.Loading
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Selesai",
                value = stats?.done?.toString() ?: "-",
                isLoading = uiState.stats is StatsUIState.Loading
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Belum",
                value = stats?.pending?.toString() ?: "-",
                isLoading = uiState.stats is StatsUIState.Loading
            )
        }

        // ... sisa konten HomeScreen yang sudah ada ...
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(text = value, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
