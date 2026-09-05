package com.radiothing.ui.common

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Bottom clearance the active screen should reserve so its last row / footer
 * clears the floating dock (76dp) + MiniPlayer (~70dp) + navigation-bar inset.
 * Provided by MainActivity, which knows the actual dock/MiniPlayer visibility;
 * screens consume it instead of hardcoding guesses like 140.dp.
 */
val LocalBottomClearance = staticCompositionLocalOf<Dp> { 140.dp }
