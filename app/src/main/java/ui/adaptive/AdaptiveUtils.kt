@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@file:Suppress("DEPRECATION")   // подавляем warning про устаревший API

package com.graphene.antivirus.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================
//  Классы ширины:
//    Compact  — телефоны в портрете (< 600dp)
//    Medium   — складные (600dp..840dp), телефоны в ландшафте
//    Expanded — планшеты (> 840dp)
//
//  windowWidthSizeClass — свойство класса WindowSizeClass,
//  объявлено в его конструкторе. Доступно напрямую.
// ============================================================

val WindowSizeClass.isCompact: Boolean
    get() = windowWidthSizeClass == WindowWidthSizeClass.Compact

val WindowSizeClass.isMedium: Boolean
    get() = windowWidthSizeClass == WindowWidthSizeClass.Medium

val WindowSizeClass.isExpanded: Boolean
    get() = windowWidthSizeClass == WindowWidthSizeClass.Expanded

val WindowSizeClass.isWide: Boolean
    get() = !isCompact

@Composable
@ReadOnlyComposable
fun WindowSizeClass.contentPadding(): PaddingValues = when {
    isExpanded -> PaddingValues(horizontal = 48.dp, vertical = 40.dp)
    isMedium   -> PaddingValues(horizontal = 32.dp, vertical = 28.dp)
    else       -> PaddingValues(horizontal = 20.dp, vertical = 20.dp)
}

@Composable
@ReadOnlyComposable
fun WindowSizeClass.contentMaxWidth(): Dp = when {
    isExpanded -> 960.dp
    isMedium   -> 720.dp
    else       -> Dp.Unspecified
}

val WindowSizeClass.gridColumns: Int
    get() = when {
        isExpanded -> 3
        isMedium   -> 2
        else       -> 1
    }

@Composable
fun AdaptiveContainer(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val padding = windowSizeClass.contentPadding()
    val maxWidth = windowSizeClass.contentMaxWidth()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.TopCenter,
        content = {
            Box(
                modifier = if (maxWidth != Dp.Unspecified)
                    Modifier.widthIn(max = maxWidth).fillMaxSize()
                else
                    Modifier.fillMaxSize(),
                content = content
            )
        }
    )
}