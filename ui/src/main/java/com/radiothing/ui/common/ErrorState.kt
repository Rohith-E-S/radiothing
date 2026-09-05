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
    // Glitch by permuting the word's own letters: block glyphs (█▓▒░) are
    // missing from Ndot57 and fell back to the system font, jittering the
    // headline's width/baseline on every swap. Letters only — no reflow.
    var glitchText by remember { mutableStateOf("ERROR") }

    // A permanent 100ms recomposition loop ignores the system-wide
    // "remove animations" setting — skip the glitch when animations are off
    val reducedMotion = rememberReducedMotion()
    LaunchedEffect(Unit) {
        if (reducedMotion) return@LaunchedEffect
        while (true) {
            delay(100)
            glitchText = if (Random.nextFloat() > 0.8f) {
                "ERROR".toList().shuffled(Random).joinToString("")
            } else {
                "ERROR"
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
        // Decorative "▓▒░" fades drawn on canvas — the text glyphs fell back
        // to the system font and jittered against the Ndot57 letters
        Row(verticalAlignment = Alignment.CenterVertically) {
            com.radiothing.ui.components.BlockFade(color = BrightRed)
            Spacer(Modifier.width(10.dp))
            Text(
                text = glitchText,
                color = BrightRed,
                fontFamily = Ndot57,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(10.dp))
            com.radiothing.ui.components.BlockFade(color = BrightRed)
        }
        
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
