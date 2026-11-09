package com.graphene.antivirus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.graphene.antivirus.data.*
import com.graphene.antivirus.ui.components.GrapheneCard
import com.graphene.antivirus.ui.components.PrimaryButton
import com.graphene.antivirus.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SyncScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var running by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<SyncResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrapheneBlack)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, null, tint = GrapheneTextPrimary)
            }
            Spacer(Modifier.width(8.dp))
            Text("Синхронизация", style = MaterialTheme.typography.titleLarge, color = GrapheneTextPrimary)
        }

        Spacer(Modifier.height(20.dp))

        GrapheneCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Sync, null, tint = GrapheneAccent)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Запустить синхронизацию сейчас",
                    style = MaterialTheme.typography.titleMedium,
                    color = GrapheneTextPrimary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Сканирует разрешённые папки, отправляет список и забирает запросы администратора лаборатории.",
                style = MaterialTheme.typography.bodyMedium,
                color = GrapheneTextSecondary
            )
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = if (running) "Выполняется…" else "Синхронизировать",
                onClick = {
                    if (running) return@PrimaryButton
                    running = true
                    scope.launch {
                        try {
                            val consent = ConsentStore(ctx)
                            val deviceId = consent.getOrCreateDeviceId()
                            val folderStore = SyncFolderStore(ctx)
                            val repo = SyncRepository(ctx, ApiClient.create(), folderStore)
                            lastResult = repo.syncAll(deviceId)
                        } finally {
                            running = false
                        }
                    }
                },
                enabled = !running,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        lastResult?.let { r ->
            GrapheneCard(Modifier.fillMaxWidth()) {
                Text("Результат", style = MaterialTheme.typography.titleMedium, color = GrapheneTextPrimary)
                Spacer(Modifier.height(8.dp))
                if (r.error != null) {
                    Text("Ошибка: ${r.error}", style = MaterialTheme.typography.bodyMedium, color = GrapheneDanger)
                } else {
                    InfoRow("Папок", r.folders.toString())
                    InfoRow("Файлов проиндексировано", r.filesIndexed.toString())
                    InfoRow("Файлов отправлено по запросу", r.filesSent.toString())
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = GrapheneTextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = GrapheneTextPrimary)
    }
}