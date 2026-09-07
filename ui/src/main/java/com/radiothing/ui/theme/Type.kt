package com.radiothing.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.radiothing.ui.R

// DotMatrix — Doto, a SIL OFL-licensed dot-matrix display face (© 2024 The Doto
// Project Authors, see licenses/Doto-OFL.txt). Shipped as one variable TTF; each
// FontWeight is pinned through the wght axis with the roundness axis maxed
// (ROND 100) for the round-dot instrument look — the file's own default instance
// is Black with square dots, so both axes are always set explicitly. Axis weights
// sit one step above the requested weights so the rendered density matches the
// app's original display face, which only shipped a single (regular) weight.
@OptIn(ExperimentalTextApi::class)
private fun dotMatrixFont(requested: FontWeight, axisWeight: Int) = Font(
    R.font.doto_variable,
    weight = requested,
    variationSettings = FontVariation.Settings(
        FontVariation.Setting("ROND", 100f),
        FontVariation.Setting("wght", axisWeight.toFloat())
    )
)

val DotMatrix = FontFamily(
    dotMatrixFont(FontWeight.Normal, 500),
    dotMatrixFont(FontWeight.Medium, 500),
    dotMatrixFont(FontWeight.SemiBold, 600),
    dotMatrixFont(FontWeight.Bold, 600),
    dotMatrixFont(FontWeight.ExtraBold, 700),
    dotMatrixFont(FontWeight.Black, 700)
)

val TitleFont = DotMatrix
val MonoFont = DotMatrix
val BodyFont = FontFamily.Default
val DisplayFont = DotMatrix

val RadioTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        letterSpacing = 3.sp,
        lineHeight = 48.sp
    ),
    displayMedium = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        letterSpacing = 2.5.sp,
        lineHeight = 36.sp
    ),
    displaySmall = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 2.sp,
        lineHeight = 30.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 1.5.sp,
        lineHeight = 26.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 1.2.sp,
        lineHeight = 22.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 1.sp,
        lineHeight = 18.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        letterSpacing = 1.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.8.sp
    ),
    titleSmall = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.8.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 19.sp
    ),
    bodySmall = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 0.6.sp
    ),
    labelLarge = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.4.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 1.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        letterSpacing = 0.8.sp
    )
)
