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
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.Ndot57
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

    // A permanent 100ms recomposition loop ignores the system-wide
    // "remove animations" setting — skip the glitch when animations are off
    val reducedMotion = rememberReducedMotion()
    LaunchedEffect(Unit) {
        if (reducedMotion) return@LaunchedEffect
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
            color = BrightRed,
            fontFamily = Ndot57,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            color = BrightRed,
            fontFamily = Ndot57,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .border(BorderStroke(2.dp, BrightRed))
                .clickable(onClickLabel = "Retry", onClick = onRetry)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "[ RETRY ]",
                color = BrightRed,
                fontFamily = Ndot57,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
