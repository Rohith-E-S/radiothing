package com.radiothing.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glitchChars = listOf('░', '▒', '▓', '█')
    var glitchText by remember { mutableStateOf("ERROR") }

    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            if (Random.nextFloat() > 0.8f) {
                glitchText = (1..5).map { glitchChars.random() }.joinToString("")
            } else {
                glitchText = "ERROR"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "█▓▒░ $glitchText ░▒▓█",
            color = Color(0xFFFF2D2D),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            color = Color(0xFFFF2D2D),
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .border(BorderStroke(2.dp, Color(0xFFFF2D2D)))
                .clickable(onClick = onRetry)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "[ RETRY ]",
                color = Color(0xFFFF2D2D),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
