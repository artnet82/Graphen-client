package com.graphene.antivirus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.graphene.antivirus.ui.components.GrapheneCard
import com.graphene.antivirus.ui.theme.*

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize().background(GrapheneBlack)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, null, tint = GrapheneTextPrimary)
            }
            Spacer(Modifier.width(8.dp))
            Text("О приложении", style = MaterialTheme.typography.titleLarge, color = GrapheneTextPrimary)
        }
        Spacer(Modifier.height(24.dp))

        GrapheneCard(Modifier.fillMaxWidth()) {
            Text("Графен", style = MaterialTheme.typography.titleLarge, color = GrapheneTextPrimary)
            Spacer(Modifier.height(8.dp))
            InfoRow("Версия", "1.2.0-lab")
            InfoRow("Режим", "Закрытая лаборатория")
            InfoRow("Архитектура", "Гибридный облачный анализ")
            InfoRow("Сеть", "DNS-монитор через локальный VPN")
            InfoRow("Автопроверка", "Каждые 15 минут")
            Spacer(Modifier.height(12.dp))
            Text(
                "Сервер анализа предустановлен разработчиком. Изменение адреса недоступно в лабораторной сборке.",
                style = MaterialTheme.typography.bodyMedium, color = GrapheneTextTertiary
            )
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