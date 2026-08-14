package com.radiothing.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.radiothing.ui.theme.RadioColors

@Composable
fun AsciiArtText(
    text: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val blockChar = "█"
    val halfBlock = "▄"
    val space = " "
    
    // Simple 3x3 block font mapping for demonstration
    fun getCharMap(c: Char): List<String> {
        return when (c.uppercaseChar()) {
            'A' -> listOf("███", "█ █", "███", "█ █", "█ █")
            'B' -> listOf("██ ", "█ █", "██ ", "█ █", "██ ")
            'C' -> listOf(" ██", "█  ", "█  ", "█  ", " ██")
            'R' -> listOf("██ ", "█ █", "██ ", "█ █", "█ █")
            'D' -> listOf("██ ", "█ █", "█ █", "█ █", "██ ")
            'I' -> listOf("███", " █ ", " █ ", " █ ", "███")
            'O' -> listOf("███", "█ █", "█ █", "█ █", "███")
            else -> listOf("███", "███", "███", "███", "███") // fallback block
        }
    }

    val displayChars = text.take(8) // Limit to prevent overflow
    val rows = mutableListOf("", "", "", "", "")
    
    if (compact) {
       // Simplified logic for compact
       Text(
           text = text.uppercase(),
           modifier = modifier,
           fontFamily = FontFamily.Monospace,
           color = RadioColors.TextPrimary,
           fontSize = 24.sp,
           fontWeight = FontWeight.Black
       )
    } else {
        for (c in displayChars) {
            val charLines = getCharMap(c)
            for (i in 0 until 5) {
                rows[i] += charLines[i] + "  "
            }
        }
        
        Text(
            text = rows.joinToString("\n"),
            modifier = modifier,
            fontFamily = FontFamily.Monospace,
            color = RadioColors.TextPrimary,
            fontSize = 10.sp,
            lineHeight = 10.sp
        )
    }
}
