package com.graphene.antivirus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.graphene.antivirus.ui.adaptive.AdaptiveContainer
import com.graphene.antivirus.ui.adaptive.isWide
import com.graphene.antivirus.ui.components.*
import com.graphene.antivirus.ui.theme.*
import com.graphene.antivirus.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    vm: MainViewModel,
    windowSizeClass: WindowSizeClass,
    onStartScan: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSyncSetup: () -> Unit = {},
    onOpenSync: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrapheneBlack)
    ) {
        AdaptiveContainer(windowSizeClass) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // ============================================================
                //  Header: GRAPHENE / ANTIVIRUS
                // ============================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "GRAPHENE",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = GrapheneTextPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "ANTIVIRUS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = GrapheneAccent,
                            letterSpacing = 4.sp
                        )
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

                Spacer(Modifier.height(24.dp))

                // ============================================================
                //  Главный блок + статы. На планшете — в строке.
                // ============================================================
                if (windowSizeClass.isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        ScannerOrbCard(
                            modifier = Modifier.weight(2f),
                            onStartScan = onStartScan
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Rounded.Cloud,
                                title = "Облако",
                                value = "Online"
                            )
                            StatCard(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Rounded.BatteryChargingFull,
                                title = "Нагрузка",
                                value = "<1%"
                            )
                        }
                    }
                } else {
                    ScannerOrbCard(onStartScan = onStartScan)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Cloud,
                            title = "Облако",
                            value = "Online"
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.BatteryChargingFull,
                            title = "Нагрузка",
                            value = "<1%"
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ============================================================
                //  Синхронизация + Сеть. На планшете — рядом.
                // ============================================================
                if (windowSizeClass.isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        NetworkCard(modifier = Modifier.weight(1f))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ActionTile(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Rounded.Folder,
                                title = "Настроить папки",
                                subtitle = "Режим лаборатории",
                                onClick = onOpenSyncSetup
                            )
                            ActionTile(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Rounded.Sync,
                                title = "Синхронизация",
                                subtitle = "Сейчас",
                                onClick = onOpenSync
                            )
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Folder,
                            title = "Настроить папки",
                            subtitle = "Режим лаборатории",
                            onClick = onOpenSyncSetup
                        )
                        ActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Sync,
                            title = "Синхронизация",
                            subtitle = "Сейчас",
                            onClick = onOpenSync
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    NetworkCard(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

// ============================================================
//  Компоненты
// ============================================================

@Composable
private fun ScannerOrbCard(
    modifier: Modifier = Modifier,
    onStartScan: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(GrapheneSurfaceHigh, GrapheneSurface)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScannerOrb(Modifier.size(180.dp), scanning = true, progress = 1f)
            Spacer(Modifier.height(20.dp))
            Text(
                "Система защищена",
                style = MaterialTheme.typography.headlineMedium,
                color = GrapheneTextPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "GRAPHENE ANTIVIRUS · мониторинг активен",
                style = MaterialTheme.typography.bodyMedium,
                color = GrapheneTextSecondary
            )
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = "Запустить проверку",
                onClick = onStartScan,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NetworkCard(modifier: Modifier = Modifier) {
    GrapheneCard(modifier) {
        Text(
            "Сетевой монитор",
            style = MaterialTheme.typography.titleLarge,
            color = GrapheneTextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Весь DNS-трафик проходит через локальный VPN-туннель GRAPHENE ANTIVIRUS. " +
                "Домены, к которым обращаются приложения, отправляются на сервер для анализа " +
                "на мошеннические паттерны.",
            style = MaterialTheme.typography.bodyMedium,
            color = GrapheneTextSecondary
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    accent: Color = GrapheneAccent
) {
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

@Composable
private fun ActionTile(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(GrapheneSurface)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Icon(icon, null, tint = GrapheneAccent, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = GrapheneTextPrimary)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = GrapheneTextTertiary)
    }
}