package com.graphene.antivirus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val Scheme = darkColorScheme(
    primary = GrapheneAccent,
    onPrimary = GrapheneBlack,
    background = GrapheneBlack,
    onBackground = GrapheneTextPrimary,
    surface = GrapheneSurface,
    onSurface = GrapheneTextPrimary,
    surfaceVariant = GrapheneSurfaceHigh,
    onSurfaceVariant = GrapheneTextSecondary,
    outline = GrapheneBorder,
    error = GrapheneDanger
)

@Composable
fun GrapheneTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = GrapheneTypography, content = content)
}