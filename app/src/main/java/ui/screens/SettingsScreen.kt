package com.graphene.antivirus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.graphene.antivirus.ui.adaptive.AdaptiveContainer
import com.graphene.antivirus.ui.adaptive.isWide
import com.graphene.antivirus.ui.components.GrapheneCard
import com.graphene.antivirus.ui.theme.*

@Composable
fun SettingsScreen(
    windowSizeClass: WindowSizeClass,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(GrapheneBlack)
    ) {
        AdaptiveContainer(windowSizeClass) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // ============================================================
                //  Хедер
                // ============================================================
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = GrapheneTextPrimary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "О приложении",
                        style = MaterialTheme.typography.titleLarge,
                        color = GrapheneTextPrimary
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ============================================================
                //  Бренд-блок
                // ============================================================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "GRAPHENE",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = GrapheneTextPrimary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "ANTIVIRUS",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GrapheneAccent,
                        letterSpacing = 6.sp
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ============================================================
                //  Карточка информации. На планшете — две в ряд.
                // ============================================================
                if (windowSizeClass.isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        InfoCard(modifier = Modifier.weight(1f))
                        ServerCard(modifier = Modifier.weight(1f))
                    }
                } else {
                    InfoCard(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    ServerCard(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun InfoCard(modifier: Modifier = Modifier) {
    GrapheneCard(modifier) {
        Text(
            "Сборка",
            style = MaterialTheme.typography.titleLarge,
            color = GrapheneTextPrimary
        )
        Spacer(Modifier.height(12.dp))
        InfoRow("Версия", "1")
        InfoRow("Режим", "Безопасный")
        InfoRow("Архитектура", "Гибридный облачный ИИ анализ")
        InfoRow("Сеть", "DNS-монитор через локальный VPN")
        InfoRow("Автопроверка", "Каждые 15 минут")
        InfoRow("Синхронизация", "Каждые 30 минут")
    }
}

@Composable
private fun ServerCard(modifier: Modifier = Modifier) {
    GrapheneCard(modifier) {
        Text(
            "Сервер",
            style = MaterialTheme.typography.titleLarge,
            color = GrapheneTextPrimary
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Адрес сервера анализа предустановлен разработчиком. Изменение недоступно " +
                "в GRAPHENE ANTIVIRUS.",
            style = MaterialTheme.typography.bodyMedium,
            color = GrapheneTextTertiary
        )
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