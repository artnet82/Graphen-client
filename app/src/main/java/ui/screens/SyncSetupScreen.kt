package com.graphene.antivirus.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.graphene.antivirus.data.ConsentStore
import com.graphene.antivirus.data.SyncFolder
import com.graphene.antivirus.data.SyncFolderStore
import com.graphene.antivirus.ui.adaptive.AdaptiveContainer
import com.graphene.antivirus.ui.adaptive.isWide
import com.graphene.antivirus.ui.components.GrapheneCard
import com.graphene.antivirus.ui.components.PrimaryButton
import com.graphene.antivirus.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun SyncSetupScreen(
    windowSizeClass: WindowSizeClass,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val folderStore = remember { SyncFolderStore(ctx) }
    val consent = remember { ConsentStore(ctx) }

    var folders by remember { mutableStateOf<List<SyncFolder>>(emptyList()) }
    var enabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { folderStore.foldersFlow.collectLatest { folders = it } }
    LaunchedEffect(Unit) { consent.syncEnabledFlow.collectLatest { enabled = it } }

    val treeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                ctx.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val name = resolveDisplayName(ctx, uri)
                scope.launch { folderStore.addFolder(uri.toString(), name) }
            } catch (_: Exception) { }
        }
    }

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
                    Column {
                        Text(
                            "Синхронизация папок",
                            style = MaterialTheme.typography.titleLarge,
                            color = GrapheneTextPrimary
                        )
                        Text(
                            "GRAPHENE ANTIVIRUS · лаборатория",
                            style = MaterialTheme.typography.labelSmall,
                            color = GrapheneAccent
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ============================================================
                //  Переключатель лабораторного режима
                // ============================================================
                GrapheneCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Режим лаборатории",
                                style = MaterialTheme.typography.titleMedium,
                                color = GrapheneTextPrimary
                            )
                            Text(
                                "Разрешить серверу GRAPHENE ANTIVIRUS запрашивать файлы " +
                                    "из выбранных папок",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrapheneTextSecondary
                            )
                        }
                        Switch(
                            checked = enabled,
                            onCheckedChange = { v ->
                                scope.launch {
                                    consent.setSyncEnabled(v)
                                    if (v && folders.isEmpty()) treeLauncher.launch(null)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = GrapheneAccent)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ============================================================
                //  Карточка «Разрешённые папки». На планшете — рядом с «Что происходит».
                // ============================================================
                val foldersCard: @Composable (Modifier) -> Unit = { mod ->
                    GrapheneCard(mod) {
                        Text(
                            "Разрешённые папки (${folders.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = GrapheneTextPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Сервер GRAPHENE ANTIVIRUS увидит только файлы из этих папок " +
                                "и сможет запросить их для анализа. Остальное содержимое " +
                                "телефона недоступно.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GrapheneTextSecondary
                        )
                        Spacer(Modifier.height(16.dp))

                        if (folders.isEmpty()) {
                            Text(
                                "Пока не выбрано ни одной папки",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrapheneTextTertiary
                            )
                        } else {
                            folders.forEach { f ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GrapheneSurfaceHigh)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Folder, null, tint = GrapheneAccent)
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        f.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = GrapheneTextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        scope.launch { folderStore.removeFolder(f.uri) }
                                    }) {
                                        Icon(
                                            Icons.Rounded.Close, null,
                                            tint = GrapheneTextTertiary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        PrimaryButton(
                            text = "Добавить папку",
                            onClick = { treeLauncher.launch(null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                val explainCard: @Composable (Modifier) -> Unit = { mod ->
                    GrapheneCard(mod) {
                        Text(
                            "Что происходит при синхронизации",
                            style = MaterialTheme.typography.titleMedium,
                            color = GrapheneTextPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        Bullet("Каждые 30 минут клиент сканирует только эти папки")
                        Bullet("Сервер получает список файлов: имя, размер, SHA-256, MIME")
                        Bullet("Администратор лаборатории может запросить любой файл из списка")
                        Bullet("При следующей синхронизации запрошенные файлы уйдут на сервер " +
                            "и будут проанализированы")
                        Bullet("Всё, что вне разрешённых папок, — недоступно")
                    }
                }

                if (windowSizeClass.isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        foldersCard(Modifier.weight(1f))
                        explainCard(Modifier.weight(1f))
                    }
                } else {
                    foldersCard(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    explainCard(Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun Bullet(text: String) {
    Row(Modifier.padding(vertical = 4.dp)) {
        Text("—", color = GrapheneAccent)
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = GrapheneTextSecondary)
    }
}

private fun resolveDisplayName(ctx: Context, uri: Uri): String {
    return try {
        DocumentsContract.getTreeDocumentId(uri).substringAfterLast(':').ifBlank { "Папка" }
    } catch (_: Exception) { "Папка" }
}