package com.radiothing.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object RadioShapes {
    // BLACK LAB: enclosures are mil-spec — 16dp outer, 12dp inner, 1dp hairline chrome.
    val Enclosure = RoundedCornerShape(18.dp)
    val Card = RoundedCornerShape(16.dp)
    val CardInner = RoundedCornerShape(12.dp)
    val Chip = RoundedCornerShape(6.dp)
    val Pill = RoundedCornerShape(100.dp)
    val Meter = RoundedCornerShape(12.dp)
    val Dial = RoundedCornerShape(20.dp)
}
