package com.graphene.antivirus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.graphene.antivirus.ui.theme.*

@Composable
fun GrapheneCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(GrapheneSurface)
            .border(1.dp, GrapheneBorder, RoundedCornerShape(20.dp))
            .padding(20.dp),
        content = content
    )
}

@Composable
fun PrimaryButton(
    text: String, onClick: () -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) Brush.horizontalGradient(listOf(GrapheneAccent, Color(0xFF4FD1A3)))
                else Brush.horizontalGradient(listOf(GrapheneSurfaceHigh, GrapheneSurfaceHigh))
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) GrapheneBlack else GrapheneTextTertiary,
            fontWeight = FontWeight.Bold
        )
    }
}