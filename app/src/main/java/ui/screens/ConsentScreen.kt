package com.graphene.antivirus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.graphene.antivirus.ui.adaptive.AdaptiveContainer
import com.graphene.antivirus.ui.adaptive.isWide
import com.graphene.antivirus.ui.components.GrapheneCard
import com.graphene.antivirus.ui.components.PrimaryButton
import com.graphene.antivirus.ui.components.ScannerOrb
import com.graphene.antivirus.ui.theme.*

@Composable
fun ConsentScreen(
    windowSizeClass: WindowSizeClass,
    onAccept: () -> Unit
) {
    var checked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GrapheneBlack, GrapheneSurface, GrapheneBlack)))
    ) {
        AdaptiveContainer(windowSizeClass) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(if (windowSizeClass.isWide) 24.dp else 32.dp))

                // ============================================================
                //  Хедер бренда
                // ============================================================
                if (windowSizeClass.isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        ScannerOrb(modifier = Modifier.size(160.dp), scanning = true)
                        Column(Modifier.weight(1f)) {
                            Text(
                                "GRAPHENE",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = GrapheneTextPrimary,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "ANTIVIRUS",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = GrapheneAccent,
                                letterSpacing = 8.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Антивирус нового поколения.\nОблачный интеллект. Нулевая нагрузка.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = GrapheneTextSecondary
                            )
                        }
                    }
                } else {
                    ScannerOrb(modifier = Modifier.size(140.dp), scanning = true)
                    Spacer(Modifier.height(24.dp))
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
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Антивирус нового поколения.\nОблачный интеллект. Нулевая нагрузка.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = GrapheneTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(28.dp))

                // ============================================================
                //  Карточка «Что анализируем»
                // ============================================================
                GrapheneCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Что анализируем",
                        style = MaterialTheme.typography.titleLarge,
                        color = GrapheneTextPrimary
                    )
                    Spacer(Modifier.height(12.dp))
                    Bullet("Приложения, их разрешения и подписи APK")
                    Bullet("Контакты, SMS и журнал вызовов — для защиты от мошенников")
                    Bullet("Исполняемые файлы и архивы в памяти устройства")
                    Bullet("DNS-трафик и домены, к которым обращаются приложения")
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Все данные передаются на сервер GRAPHENE ANTIVIRUS для анализа. " +
                            "Согласие можно отозвать в настройках.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrapheneTextTertiary
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ============================================================
                //  Чекбокс согласия
                // ============================================================
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(GrapheneSurfaceHigh)
                        .padding(16.dp)
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { checked = it },
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
                    text = "Начать защиту",
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = checked
                )

                Spacer(Modifier.height(24.dp))
            }
        }
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