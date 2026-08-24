package com.radiothing.ui.theme

import androidx.compose.ui.graphics.Color

// ── BLACK LAB world ──
// Night bench in a quiet lab. Matte anodized black absorbs light; signal red preserves it.
// Scene-forced dark: OLED black ground so red is true, amber holds the meter's memory.
val PureBlack = Color(0xFF050507)
val Ink = Color(0xFF09090B)          // navigation / system bars
val Panel = Color(0xFF121214)        // raised enclosures
val PanelHover = Color(0xFF17171A)
val GridLine = Color(0xFF232326)
val Hairline = Color(0xFF2A2A2E)
val BrightRed = Color(0xFFFF3344)    // committed accent — ~35% of surface when active
val LiveRed = Color(0xFFFF1A2D)      // peak / live
val SignalAmber = Color(0xFFFFA231)  // seven-seg secondary, tuning
val TextWhite100 = Color(0xFFFFFFFF)
val TextWhite70 = Color(0xB3FFFFFF)
val TextWhite45 = Color(0x73FFFFFF)
val TextWhite35 = Color(0x59FFFFFF)
val TextWhite40 = Color(0x66FFFFFF)
val DarkGray = Color(0xFF121214)
val BorderGray = Color(0xFF232326)

object RadioColors {
    val Background = PureBlack
    val Surface = Panel
    val SurfaceHover = PanelHover
    val Border = GridLine
    val BorderStrong = Hairline
    val Accent = BrightRed
    val LiveAccent = LiveRed
    val Amber = SignalAmber
    val TextPrimary = TextWhite100
    val TextSecondary = TextWhite70
    val TextTertiary = TextWhite45
    val TextMuted = TextWhite35

    val FavoriteAdded = Color(0xFFFF3344)
    val PlaylistAdded = Color(0xFFFFA231)
    // legacy aliases
    val InkBg = Ink
    val PanelBg = Panel
}
