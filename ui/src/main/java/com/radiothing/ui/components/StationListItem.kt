package com.radiothing.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.radiothing.ui.theme.Ndot57
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.radiothing.domain.model.RadioStation
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Hairline
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.TextWhite35
import com.radiothing.ui.theme.TextWhite70

fun countryCodeToEmoji(countryCode: String): String {
    if (countryCode.length != 2) return ""
    val firstLetter = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StationListItem(
    station: RadioStation,
    isPlaying: Boolean,
    onStationClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    // Enclosure: Panel + 1dp hairline, 16dp radius, generous air. Minimal transistor density.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Panel)
            .border(1.dp, if (isPlaying) BrightRed.copy(alpha = 0.55f) else GridLine, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onStationClick,
                onLongClick = { onFavoriteClick() },
                onLongClickLabel = "Toggle favorite"
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Artwork — art-forward discipline: 52dp when favicon exists
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0D0D0F))
                .border(1.dp, if (isPlaying) BrightRed.copy(0.45f) else Hairline, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (station.favicon.isNotEmpty()) {
                AsyncImage(
                    model = station.favicon,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Text(
                    text = station.name.take(2).uppercase(),
                    color = Color.White,
                    fontFamily = Ndot57,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(BrightRed)
                        .border(1.dp, Color.White.copy(0.9f), CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(13.dp))

        // Station info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPlaying) {
                    val infiniteTransition = rememberInfiniteTransition(label = "playing")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.35f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(700),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(BrightRed.copy(alpha = alpha))
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                }
                Text(
                    text = station.name,
                    color = Color.White,
                    fontFamily = Ndot57,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 0.3.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (station.countryCode.length == 2) {
                    Text(
                        text = countryCodeToEmoji(station.countryCode),
                        fontSize = 11.sp
                    )
                }

                if (station.bitrate > 0) {
                    Text(
                        text = "${station.bitrate}k",
                        color = BrightRed,
                        fontFamily = Ndot57,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        modifier = Modifier
                            .border(1.dp, BrightRed.copy(alpha = 0.5f), RoundedCornerShape(5.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                } else {
                    Text(
                        text = station.codec.uppercase().takeIf { it.isNotEmpty() } ?: "LIVE",
                        color = TextWhite35,
                        fontFamily = Ndot57,
                        fontSize = 9.sp,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier
                            .background(Color(0xFF1C1C1F), RoundedCornerShape(5.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                val tags = station.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(2)
                tags.forEach { tag ->
                    Text(
                        text = tag.uppercase(),
                        color = TextWhite35,
                        fontFamily = Ndot57,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        modifier = Modifier
                            .background(Color(0xFF1C1C1F), RoundedCornerShape(5.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            if (station.votes > 0) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "♥ ${station.votes}  •  ${station.country.take(20)}",
                    color = TextWhite35,
                    fontFamily = Ndot57,
                    fontSize = 9.sp,
                    letterSpacing = 0.4.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Favorite — 48dp touch, no circle halo — just the heart, red when favorited
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = if (station.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (station.isFavorite) "Remove favorite" else "Add favorite",
                tint = if (station.isFavorite) BrightRed else TextWhite35,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
