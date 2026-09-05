package com.radiothing.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class IconType {
    BROWSE, FAVORITES, PLAYLISTS, HISTORY, SETTINGS, FILTER,
    PLAY, PAUSE, PREV, NEXT
}


private fun iconMatrix(type: IconType): List<List<Int>> = when (type) {
        IconType.BROWSE -> listOf(
            listOf(0, 1, 1, 1, 0),
            listOf(1, 0, 0, 0, 1),
            listOf(1, 0, 1, 0, 1),
            listOf(1, 0, 0, 0, 1),
            listOf(0, 1, 1, 1, 0)
        )
        IconType.FAVORITES -> listOf(
            listOf(0, 1, 0, 1, 0),
            listOf(1, 1, 1, 1, 1),
            listOf(1, 1, 1, 1, 1),
            listOf(0, 1, 1, 1, 0),
            listOf(0, 0, 1, 0, 0)
        )
        // Playlists — stacked tapes/rows in a well, distinct from the favorites heart
        IconType.PLAYLISTS -> listOf(
            listOf(1, 1, 1, 1, 1),
            listOf(1, 0, 0, 0, 1),
            listOf(1, 1, 1, 1, 1),
            listOf(1, 0, 0, 0, 1),
            listOf(1, 1, 1, 1, 1)
        )
        IconType.HISTORY -> listOf(
            listOf(0, 1, 1, 1, 0),
            listOf(1, 0, 1, 0, 1),
            listOf(1, 0, 1, 1, 0),
            listOf(1, 0, 0, 0, 1),
            listOf(0, 1, 1, 1, 0)
        )
        IconType.SETTINGS -> listOf(
            listOf(1, 0, 1, 0, 1),
            listOf(0, 1, 1, 1, 0),
            listOf(1, 1, 0, 1, 1),
            listOf(0, 1, 1, 1, 0),
            listOf(1, 0, 1, 0, 1)
        )
        // ASCII-art funnel — wide mouth narrowing to a stem, reads as "filter" in dot language
        IconType.FILTER -> listOf(
            listOf(1, 1, 1, 1, 1),
            listOf(0, 1, 1, 1, 0),
            listOf(0, 0, 1, 0, 0),
            listOf(0, 0, 1, 0, 0),
            listOf(0, 0, 1, 0, 0)
        )
        // Play — right-pointing triangle in 5x5
        IconType.PLAY -> listOf(
            listOf(0, 0, 1, 0, 0),
            listOf(0, 0, 1, 1, 0),
            listOf(0, 0, 1, 1, 1),
            listOf(0, 0, 1, 1, 0),
            listOf(0, 0, 1, 0, 0)
        )
        // Pause — two vertical bars
        IconType.PAUSE -> listOf(
            listOf(0, 1, 0, 1, 0),
            listOf(0, 1, 0, 1, 0),
            listOf(0, 1, 0, 1, 0),
            listOf(0, 1, 0, 1, 0),
            listOf(0, 1, 0, 1, 0)
        )
        // Prev — left-pointing triangle + bar on the right
        IconType.PREV -> listOf(
            listOf(0, 0, 1, 0, 1),
            listOf(0, 1, 1, 0, 1),
            listOf(1, 1, 1, 0, 1),
            listOf(0, 1, 1, 0, 1),
            listOf(0, 0, 1, 0, 1)
        )
        // Next — bar on the left + right-pointing triangle
        IconType.NEXT -> listOf(
            listOf(1, 0, 1, 0, 0),
            listOf(1, 0, 1, 1, 0),
            listOf(1, 0, 1, 1, 1),
            listOf(1, 0, 1, 1, 0),
            listOf(1, 0, 1, 0, 0)
        )
}

@Composable
fun DotMatrixIcon(
    type: IconType,
    size: Dp = 24.dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    // 5x5 grid representation for icons. remember(type) so the matrix lists
    // are built once per icon, not on every recomposition of every tab.
    val matrix = remember(type) { iconMatrix(type) }
    Canvas(modifier = modifier.size(size)) {
        val dotRadius = this.size.width / 12f
        val spacing = this.size.width / 5f
        val startX = spacing / 2f
        val startY = spacing / 2f

        for (row in 0 until 5) {
            for (col in 0 until 5) {
                if (matrix[row][col] == 1) {
                    drawCircle(
                        color = color,
                        radius = dotRadius,
                        center = Offset(
                            x = startX + col * spacing,
                            y = startY + row * spacing
                        )
                    )
                }
            }
        }
    }
}
