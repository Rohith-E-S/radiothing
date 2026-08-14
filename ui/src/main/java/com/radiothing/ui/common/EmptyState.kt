package com.radiothing.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class EmptyStateType {
    NO_RESULTS, NO_FAVORITES, NO_PLAYLISTS, NO_HISTORY
}

@Composable
fun EmptyState(
    type: EmptyStateType,
    modifier: Modifier = Modifier
) {
    val (asciiArt, title, subtitle) = when (type) {
        EmptyStateType.NO_RESULTS -> Triple(
            """
               .-.
              |   |
             _|_|_|_
            |       |
            | O   O |
            |_______|
            """.trimIndent(),
            "NO STATIONS FOUND",
            "Try a different search term"
        )
        EmptyStateType.NO_FAVORITES -> Triple(
            """
              .-"-. 
             /     \
             \     /
              `._.`
            """.trimIndent(),
            "NO FAVORITES YET",
            "Browse stations to add favorites"
        )
        EmptyStateType.NO_PLAYLISTS -> Triple(
            """
             ______
            |      |
            |      |
            |______|
            """.trimIndent(),
            "NO PLAYLISTS YET",
            "Create a playlist to organize stations"
        )
        EmptyStateType.NO_HISTORY -> Triple(
            """
              .--.
             |  _ |
             | | \|
              `--'
            """.trimIndent(),
            "NO HISTORY YET",
            "Start listening to see history"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = asciiArt,
            color = Color(0xFF666666),
            fontFamily = FontFamily.Monospace,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            color = Color(0xFF666666),
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = subtitle,
            color = Color(0xFF666666),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
