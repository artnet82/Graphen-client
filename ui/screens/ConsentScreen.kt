package com.graphene.antivirus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.graphene.antivirus.ui.components.GrapheneCard
import com.graphene.antivirus.ui.components.PrimaryButton
import com.graphene.antivirus.ui.components.ScannerOrb
import com.graphene.antivirus.ui.theme.*

@Composable
fun ConsentScreen(onAccept: () -> Unit) {
    var checked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GrapheneBlack, GrapheneSurface, GrapheneBlack)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScannerOrb(modifier = Modifier.size(160.dp), scanning = true)
        Spacer(Modifier.height(32.dp))
        Text("ГРАФЕН", style = MaterialTheme.typography.displayLarge, color = GrapheneTextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            "Антивирус нового поколения.\nОблачный интеллект. Нулевая нагрузка.",
            style = MaterialTheme.typography.bodyLarge,
            color = GrapheneTextSecondary, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        GrapheneCard(modifier = Modifier.fillMaxWidth()) {
            Text("Что мы анализируем", style = MaterialTheme.typography.titleLarge, color = GrapheneTextPrimary)
            Spacer(Modifier.height(12.dp))
            Bullet("Приложения, их разрешения и подписи APK")
            Bullet("Контакты, SMS и журнал вызовов — для защиты от мошенников")
            Bullet("Исполняемые файлы и архивы в памяти устройства")
            Bullet("DNS-трафик и домены, к которым обращаются приложения")
            Spacer(Modifier.height(12.dp))
            Text(
                "Все данные передаются на сервер Графен для анализа. Согласие можно отозвать.",
                style = MaterialTheme.typography.bodyMedium,
                color = GrapheneTextTertiary
            )
        }

        Spacer(Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(GrapheneSurfaceHigh)
                .padding(16.dp)
        ) {
            Checkbox(
                checked = checked, onCheckedChange = { checked = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = GrapheneAccent,
                    uncheckedColor = GrapheneTextTertiary,
                    checkmarkColor = GrapheneBlack
                )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Согласен на обработку телеметрии для защиты устройства",
                style = MaterialTheme.typography.bodyMedium,
                color = GrapheneTextPrimary
            )
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = "Начать защиту", onClick = onAccept,
            modifier = Modifier.fillMaxWidth(), enabled = checked
        )
    }
}

@Composable
private fun Bullet(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("—", color = GrapheneAccent, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = GrapheneTextSecondary)
    }
}