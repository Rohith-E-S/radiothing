package com.radiothing.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class IconType {
    BROWSE, FAVORITES, HISTORY, SETTINGS
}

@Composable
fun DotMatrixIcon(
    type: IconType,
    size: Dp = 24.dp,
    color: Color,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 5x5 grid representation for icons
    val matrix = when (type) {
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
    }

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
