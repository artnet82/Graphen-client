package com.graphene.antivirus.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.windowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Классы ширины:
 *   Compact  — телефоны в портрете (< 600dp)
 *   Medium   — складные (600dp..840dp) и телефоны в ландшафте
 *   Expanded — планшеты (> 840dp)
 */
val WindowSizeClass.isCompact: Boolean
    get() = windowWidthSizeClass == WindowWidthSizeClass.Compact

val WindowSizeClass.isMedium: Boolean
    get() = windowWidthSizeClass == WindowWidthSizeClass.Medium

val WindowSizeClass.isExpanded: Boolean
    get() = windowWidthSizeClass == WindowWidthSizeClass.Expanded

val WindowSizeClass.isWide: Boolean
    get() = !isCompact

/** Горизонтальные/вертикальные отступы контента под каждый класс окна. */
@Composable
@ReadOnlyComposable
fun WindowSizeClass.contentPadding(): PaddingValues = when {
    isExpanded -> PaddingValues(horizontal = 48.dp, vertical = 40.dp)
    isMedium   -> PaddingValues(horizontal = 32.dp, vertical = 28.dp)
    else       -> PaddingValues(horizontal = 20.dp, vertical = 20.dp)
}

/** Максимальная ширина контента: планшет не растягивает карточки на всю ширину. */
@Composable
@ReadOnlyComposable
fun WindowSizeClass.contentMaxWidth(): Dp = when {
    isExpanded -> 960.dp
    isMedium   -> 720.dp
    else       -> Dp.Unspecified
}

/** Сколько колонок использовать в сетке карточек. */
val WindowSizeClass.gridColumns: Int
    get() = when {
        isExpanded -> 3
        isMedium   -> 2
        else       -> 1
    }

/**
 * Универсальный контейнер: отступы по классу ширины + центрирование с
 * ограничением максимальной ширины. Использовать на всех экранах.
 */
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