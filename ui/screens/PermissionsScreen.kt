package com.graphene.antivirus.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.graphene.antivirus.ui.theme.*

private data class PermissionItem(
    val key: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val check: (Context) -> Boolean,
    val request: (Context) -> Unit
)

@Composable
fun PermissionsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var refresh by remember { mutableStateOf(0) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { refresh++ }

    val items = remember(refresh) { buildPermissionList(ctx, launcher::launch) }

    Column(
        modifier = Modifier.fillMaxSize().background(GrapheneBlack).padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, null, tint = GrapheneTextPrimary)
            }
            Spacer(Modifier.width(8.dp))
            Text("Доступы", style = MaterialTheme.typography.titleLarge, color = GrapheneTextPrimary)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Графену нужен полный доступ к системе для защиты от мошенников. Все данные уходят только на предустановленный сервер Графена.",
            style = MaterialTheme.typography.bodyMedium, color = GrapheneTextSecondary
        )
        Spacer(Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items) { p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GrapheneSurface)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val granted = p.check(ctx)
                    Icon(
                        p.icon, null,
                        tint = if (granted) GrapheneAccent else GrapheneTextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.title, style = MaterialTheme.typography.titleMedium, color = GrapheneTextPrimary)
                        Text(p.description, style = MaterialTheme.typography.bodyMedium, color = GrapheneTextSecondary)
                    }
                    if (granted) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = GrapheneAccent)
                    } else {
                        TextButton(onClick = { p.request(ctx); refresh++ }) {
                            Text("Дать", color = GrapheneAccent)
                        }
                    }
                }
            }
        }
    }
}

private fun buildPermissionList(
    ctx: Context,
    requestMultiple: (Array<String>) -> Unit
): List<PermissionItem> {
    fun checked(perm: String) = ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED

    return listOf(
        PermissionItem(
            key = "contacts",
            title = "Контакты",
            description = "Выявление подмены контактов мошенниками",
            icon = Icons.Rounded.Contacts,
            check = { checked(Manifest.permission.READ_CONTACTS) },
            request = { requestMultiple(arrayOf(Manifest.permission.READ_CONTACTS)) }
        ),
        PermissionItem(
            key = "sms",
            title = "SMS",
            description = "Обнаружение фишинговых и мошеннических сообщений",
            icon = Icons.Rounded.Message,
            check = { checked(Manifest.permission.READ_SMS) },
            request = { requestMultiple(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)) }
        ),
        PermissionItem(
            key = "calls",
            title = "Журнал вызовов",
            description = "Защита от телефонного мошенничества",
            icon = Icons.Rounded.Call,
            check = { checked(Manifest.permission.READ_CALL_LOG) },
            request = { requestMultiple(arrayOf(Manifest.permission.READ_CALL_LOG)) }
        ),
        PermissionItem(
            key = "phone",
            title = "Состояние телефона",
            description = "Идентификация SIM и сетевого оператора",
            icon = Icons.Rounded.PhoneAndroid,
            check = { checked(Manifest.permission.READ_PHONE_STATE) },
            request = { requestMultiple(arrayOf(Manifest.permission.READ_PHONE_STATE)) }
        ),
        PermissionItem(
            key = "notif",
            title = "Уведомления",
            description = "Постоянное уведомление о защите",
            icon = Icons.Rounded.Notifications,
            check = {
                if (Build.VERSION.SDK_INT >= 33) checked(Manifest.permission.POST_NOTIFICATIONS)
                else true
            },
            request = {
                if (Build.VERSION.SDK_INT >= 33) requestMultiple(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            }
        ),
        PermissionItem(
            key = "files",
            title = "Все файлы",
            description = "Сканирование APK и архивов в памяти",
            icon = Icons.Rounded.Folder,
            check = {
                if (Build.VERSION.SDK_INT >= 30) Environment.isExternalStorageManager()
                else true
            },
            request = { c ->
                if (Build.VERSION.SDK_INT >= 30) {
                    val i = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    i.data = Uri.parse("package:${c.packageName}")
                    c.startActivity(i)
                }
            }
        )
    )
}