package com.graphene.antivirus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.graphene.antivirus.ui.components.*
import com.graphene.antivirus.ui.theme.*
import com.graphene.antivirus.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    vm: MainViewModel,
    onStartScan: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSyncSetup: () -> Unit = {},
    onOpenSync: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrapheneBlack)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("ГРАФЕН", style = MaterialTheme.typography.headlineLarge, color = GrapheneTextPrimary)
                Text("Независимая защита", style = MaterialTheme.typography.labelSmall, color = GrapheneAccent)
            }
            Row {
                IconButton(onClick = onOpenPermissions) {
                    Icon(Icons.Rounded.Security, null, tint = GrapheneTextSecondary)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Rounded.Tune, null, tint = GrapheneTextSecondary)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(GrapheneSurfaceHigh, GrapheneSurface)))
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScannerOrb(Modifier.size(180.dp), scanning = true, progress = 1f)
                Spacer(Modifier.height(20.dp))
                Text("Система защищена", style = MaterialTheme.typography.headlineMedium, color = GrapheneTextPrimary)
                Spacer(Modifier.height(6.dp))
                Text("Непрерывный мониторинг активен",
                    style = MaterialTheme.typography.bodyMedium, color = GrapheneTextSecondary)
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(Modifier.weight(1f), Icons.Rounded.Cloud, "Облако", "Online")
            StatCard(Modifier.weight(1f), Icons.Rounded.BatteryChargingFull, "Нагрузка", "<1%")
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton("Запустить проверку", onStartScan, Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))
        GrapheneCard(Modifier.fillMaxWidth()) {
            Text("Сетевой монитор", style = MaterialTheme.typography.titleLarge, color = GrapheneTextPrimary)
            Spacer(Modifier.height(8.dp))
            Text(
                "Весь DNS-трафик проходит через локальный VPN-туннель Графена. Домены, к которым обращаются приложения, отправляются на сервер для анализа на мошеннические паттерны.",
                style = MaterialTheme.typography.bodyMedium, color = GrapheneTextSecondary
            )
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, icon: ImageVector, title: String, value: String, accent: Color = GrapheneAccent) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(GrapheneSurface)
            .padding(16.dp)
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(12.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = GrapheneTextPrimary)
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = GrapheneTextTertiary)
    }
}