package com.graphene.antivirus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.graphene.antivirus.ui.adaptive.AdaptiveContainer
import com.graphene.antivirus.ui.adaptive.isWide
import com.graphene.antivirus.ui.components.PrimaryButton
import com.graphene.antivirus.ui.components.ScannerOrb
import com.graphene.antivirus.ui.theme.*
import com.graphene.antivirus.viewmodel.MainViewModel
import com.graphene.antivirus.viewmodel.ScanUiState

@Composable
fun ScanScreen(
    vm: MainViewModel,
    windowSizeClass: WindowSizeClass,
    onBack: () -> Unit
) {
    val state by vm.scanState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize().background(GrapheneBlack)
    ) {
        AdaptiveContainer(windowSizeClass) {
            Column(Modifier.fillMaxSize()) {
                // ============================================================
                //  Хедер
                // ============================================================
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = GrapheneTextPrimary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Проверка",
                            style = MaterialTheme.typography.titleLarge,
                            color = GrapheneTextPrimary
                        )
                        Text(
                            "GRAPHENE ANTIVIRUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = GrapheneAccent
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // ============================================================
                //  Центральный блок статуса
                // ============================================================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val s = state) {
                        is ScanUiState.Idle -> {
                            ScannerOrb(Modifier.size(180.dp), scanning = false, progress = 0f)
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Готов к проверке",
                                style = MaterialTheme.typography.headlineMedium,
                                color = GrapheneTextPrimary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Нажмите «Начать», чтобы проанализировать устройство",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrapheneTextSecondary
                            )
                        }

                        is ScanUiState.InProgress -> {
                            ScannerOrb(Modifier.size(220.dp), scanning = true, progress = s.progress)
                            Spacer(Modifier.height(32.dp))
                            Text(
                                s.step,
                                style = MaterialTheme.typography.titleMedium,
                                color = GrapheneTextPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${(s.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrapheneAccent
                            )
                        }

                        is ScanUiState.Done -> {
                            val threats = s.response.threats
                            val clean = threats.isEmpty()
                            ScannerOrb(
                                Modifier.size(200.dp),
                                scanning = false,
                                progress = 1f,
                                activeColor = if (clean) GrapheneAccent else GrapheneDanger
                            )
                            Spacer(Modifier.height(24.dp))
                            Text(
                                if (clean) "Угроз не найдено" else "Обнаружено: ${threats.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = if (clean) GrapheneTextPrimary else GrapheneDanger
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Проверено приложений: ${s.response.scannedApps}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrapheneTextSecondary
                            )
                        }

                        is ScanUiState.Error -> {
                            Icon(
                                Icons.Rounded.ErrorOutline, null,
                                tint = GrapheneDanger,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Ошибка",
                                style = MaterialTheme.typography.headlineMedium,
                                color = GrapheneDanger
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                s.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrapheneTextSecondary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ============================================================
                //  Список угроз
                // ============================================================
                val done = state as? ScanUiState.Done
                if (done != null && done.response.threats.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(done.response.threats) { t ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(GrapheneSurface)
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val color = when (t.severity) {
                                    "HIGH" -> GrapheneDanger
                                    "MEDIUM" -> GrapheneWarning
                                    else -> GrapheneTextSecondary
                                }
                                Icon(Icons.Rounded.Warning, null, tint = color)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        t.packageName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = GrapheneTextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        t.threatName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = color
                                    )
                                }
                                Text(
                                    t.severity,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // ============================================================
                //  Кнопка
                // ============================================================
                when (state) {
                    is ScanUiState.Idle ->
                        PrimaryButton("Начать", { vm.runScan() }, Modifier.fillMaxWidth())

                    is ScanUiState.Done, is ScanUiState.Error ->
                        PrimaryButton(
                            "Готово",
                            { vm.resetScan(); onBack() },
                            Modifier.fillMaxWidth()
                        )

                    else -> Unit
                }
            }
        }
    }
}