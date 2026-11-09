package com.graphene.antivirus.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.graphene.antivirus.ui.theme.GrapheneAccent
import com.graphene.antivirus.ui.theme.GrapheneBorder

@Composable
fun ScannerOrb(
    modifier: Modifier = Modifier,
    scanning: Boolean,
    progress: Float = 0f,
    activeColor: Color = GrapheneAccent
) {
    val tr = rememberInfiniteTransition(label = "orb")
    val angle by tr.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(3200, easing = LinearEasing)),
        label = "a"
    )
    val pulse by tr.animateFloat(
        0.95f, 1.04f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "p"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f - 8f
            val c = Offset(size.width / 2, size.height / 2)

            drawCircle(GrapheneBorder, r, c, style = Stroke(1.2f))
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(activeColor.copy(alpha = 0.08f * pulse), Color.Transparent),
                    center = c, radius = r
                ),
                radius = r * pulse, center = c
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(Color.Transparent, activeColor, activeColor, Color.Transparent),
                    center = c
                ),
                startAngle = angle, sweepAngle = 130f, useCenter = false,
                topLeft = Offset(c.x - r, c.y - r),
                size = Size(r * 2, r * 2),
                style = Stroke(3f, cap = StrokeCap.Round)
            )
            if (scanning && progress > 0f) {
                drawArc(
                    color = activeColor, startAngle = -90f,
                    sweepAngle = 360f * progress, useCenter = false,
                    topLeft = Offset(c.x - r + 18f, c.y - r + 18f),
                    size = Size((r - 18f) * 2, (r - 18f) * 2),
                    style = Stroke(2f, cap = StrokeCap.Round)
                )
            }
        }
        Icon(Icons.Rounded.Shield, null, tint = activeColor, modifier = Modifier.size(56.dp))
    }
}