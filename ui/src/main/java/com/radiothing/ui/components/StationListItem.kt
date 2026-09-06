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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.radiothing.ui.theme.RadioThingTheme
import com.radiothing.ui.common.rememberReducedMotion
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.radiothing.domain.model.RadioStation
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Ndot57
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.TextWhite35
import com.radiothing.ui.theme.TextWhite70

// Hoisted shapes to avoid allocation per row
private val ThumbShape = RoundedCornerShape(10.dp)
private val CardShape = RoundedCornerShape(12.dp)

fun countryCodeToEmoji(countryCode: String): String {
    if (countryCode.length != 2) return ""
    val cc = countryCode.uppercase()
    val firstLetter = Character.codePointAt(cc, 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(cc, 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}


private fun formatVotes(votes: Int): String {
    return when {
        votes >= 10000 -> "${votes / 1000}K"
        votes >= 1000 -> {
            val tenths = (votes % 1000) / 100
            if (tenths == 0) "${votes / 1000}K" else "${votes / 1000}.${tenths}K"
        }
        else -> votes.toString()
    }
}

@Composable
private fun AudioEqualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    barWidth: Dp = 2.dp,
    maxHeight: Dp = 10.dp
) {
    if (!isPlaying) {
        Row(
            modifier = modifier.height(maxHeight),
            horizontalArrangement = Arrangement.spacedBy(1.5.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            repeat(barCount) {
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .fillMaxHeight(0.2f)
                        .clip(RoundedCornerShape(0.5.dp))
                        .background(TextWhite35.copy(alpha = 0.4f))
                )
            }
        }
        return
    }

    // Respect the system-wide "remove animations" setting (Settings → Accessibility
    // → Remove animations). When enabled, skip the infinite transitions and
    // render a static equalizer — saves CPU and avoids vestibular issues.
    // rememberReducedMotion caches the read; a raw binder call here would run
    // on every recomposition of every row.
    val animatorScaleOff = rememberReducedMotion()
    if (animatorScaleOff) {
        // Static fallback — slightly varied bar heights so it doesn't look broken
        Row(
            modifier = modifier.height(maxHeight),
            horizontalArrangement = Arrangement.spacedBy(1.5.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            val staticHeights = floatArrayOf(0.6f, 0.4f, 0.75f, 0.5f)
            repeat(barCount) { i ->
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .fillMaxHeight(staticHeights[i % staticHeights.size])
                        .clip(RoundedCornerShape(0.5.dp))
                        .background(BrightRed)
                )
            }
        }
        return
    }

    // Single transition drives all bars — 2 animated values derived into 4 heights
    // reduces per-frame work at 120Hz vs 4 independent transitions.
    val transition = rememberInfiniteTransition(label = "eq_transition")
    val p1 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "p1"
    )
    val p2 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Restart),
        label = "p2"
    )
    // Derive 4 heights from 2 phases with cheap sin — no extra animatables
    val h1 = 0.25f + 0.7f * kotlin.math.abs(kotlin.math.sin((p1 * 6.28f).toDouble()).toFloat())
    val h2 = 0.3f + 0.4f * kotlin.math.abs(kotlin.math.cos((p1 * 6.28f + 1.5f).toDouble()).toFloat())
    val h3 = 0.35f + 0.65f * kotlin.math.abs(kotlin.math.sin((p2 * 6.28f + 0.8f).toDouble()).toFloat())
    val h4 = 0.2f + 0.65f * kotlin.math.abs(kotlin.math.cos((p2 * 6.28f).toDouble()).toFloat())
    val heights = listOf(h1, h2, h3, h4)

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            val scale = heights[i % heights.size]
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(scale)
                    .clip(RoundedCornerShape(0.5.dp))
                    .background(BrightRed)
            )
        }
    }
}

@Composable
private fun ArtworkThumb(
    station: RadioStation,
    isPlaying: Boolean,
    showCachedIcon: Boolean = true
) {
    val initials = remember(station.name) { station.name.take(2).uppercase() }
    val flag = remember(station.countryCode) { countryCodeToEmoji(station.countryCode) }
    val thumbBorderColor = if (isPlaying) BrightRed else Color.White.copy(alpha = 0.15f)

    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(ThumbShape)
            .background(Color(0xFF09090B))
            .border(1.dp, thumbBorderColor, ThumbShape),
        contentAlignment = Alignment.Center
    ) {
        if (station.favicon.isNotEmpty() && showCachedIcon) {
            val ctx = LocalContext.current
            val req = remember(station.favicon, station.stationUuid) {
                ImageRequest.Builder(ctx)
                    .data(station.favicon)
                    .size(112)
                    .scale(coil.size.Scale.FILL)
                    .crossfade(false)
                    .allowHardware(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCacheKey("st_${station.stationUuid}")
                    .diskCacheKey("st_${station.stationUuid}")
                    .build()
            }
            // If already in memory cache, skip placeholder entirely — no extra recomposition on scroll.
            val isInMemory = remember(station.stationUuid, showCachedIcon) {
                ctx.imageLoader.memoryCache?.get(MemoryCache.Key("st_${station.stationUuid}")) != null
            }
            var showPlaceholder by remember(isInMemory) { mutableStateOf(!isInMemory) }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (showPlaceholder) PlaceholderContent(flag, initials)
                AsyncImage(
                    model = req,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    onSuccess = { showPlaceholder = false },
                    onError = { showPlaceholder = true }
                )
            }
        } else {
            PlaceholderContent(flag, initials)
        }
    }
}


@Composable
private fun PlaceholderContent(flag: String, initials: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (flag.isNotEmpty()) {
            Text(text = flag, fontSize = 16.sp)
        } else {
            Text(
                text = initials,
                color = Color.White,
                fontFamily = Ndot57,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
fun StationListItem(
    station: RadioStation,
    isPlaying: Boolean,
    onStationClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    showIcon: Boolean = true,
    compactMode: Boolean = false,
    /** Extra row action, such as removing a station from a playlist. */
    trailingTopContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val title = remember(station.name) { station.name.trim().uppercase() }
    val metadata = remember(
        station.countryCode,
        station.country,
        station.codec,
        station.bitrate,
        station.tags
    ) {
        val location = station.countryCode
            .takeIf { it.isNotBlank() }
            ?.let { "${countryCodeToEmoji(it)} ${it.uppercase()}" }
            ?: station.country.takeIf { it.isNotBlank() }?.uppercase()
        val format = buildList {
            station.codec.takeIf { it.isNotBlank() }?.let { add(it.uppercase()) }
            if (station.bitrate > 0) add("${station.bitrate} KBPS")
        }.joinToString(" / ").ifEmpty { "LIVE STREAM" }
        val genre = station.tags
            .split(',')
            .firstOrNull { it.isNotBlank() }
            ?.trim()
            ?.uppercase()

        listOfNotNull(location, format, genre).joinToString("  •  ")
    }
    val votesLabel = remember(station.votes) { "${formatVotes(station.votes)} VOTES" }
    val borderColor = if (isPlaying) BrightRed.copy(alpha = 0.72f) else GridLine.copy(alpha = 0.7f)
    val cardBackground = if (isPlaying) Color(0xFF17171A) else Panel
    val noFontPadding = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(cardBackground)
            .border(1.dp, borderColor, CardShape)
            .clickable(onClickLabel = "Play ${station.name}", onClick = onStationClick)
    ) {
        // A receiver-style tuning rail makes the active station identifiable
        // before the user has to read any text.
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(if (isPlaying) 3.dp else 1.dp)
                    .fillMaxHeight()
                    .background(if (isPlaying) BrightRed else GridLine)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArtworkThumb(station = station, isPlaying = isPlaying, showCachedIcon = showIcon)

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 58.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontFamily = Ndot57,
                        fontSize = if (compactMode) 14.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.25.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = noFontPadding,
                        modifier = Modifier.weight(1f)
                    )

                    if (isPlaying) {
                        Spacer(Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (!compactMode) {
                                AudioEqualizerBars(
                                    isPlaying = true,
                                    barCount = 4,
                                    barWidth = 2.dp,
                                    maxHeight = 11.dp
                                )
                            }
                            Text(
                                text = "LIVE",
                                color = BrightRed,
                                fontFamily = Ndot57,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                style = noFontPadding
                            )
                        }
                    }
                }

                Text(
                    text = metadata,
                    color = TextWhite70,
                    fontFamily = Ndot57,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.25.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = noFontPadding,
                    modifier = Modifier.padding(top = 0.dp)
                )

                Text(
                    text = votesLabel,
                    color = TextWhite35,
                    fontFamily = Ndot57,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.65.sp,
                    style = noFontPadding,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(Modifier.width(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                trailingTopContent?.invoke()

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .semantics {
                            contentDescription = if (station.isFavorite) {
                                "Remove ${station.name} from favorites"
                            } else {
                                "Add ${station.name} to favorites"
                            }
                        }
                        .clickable(onClick = onFavoriteClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (station.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (station.isFavorite) BrightRed else TextWhite35,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF050507L)
@Composable
private fun StationListItemIdlePreview() {
    RadioThingTheme {
        StationListItem(
            station = previewStation(),
            isPlaying = false,
            onStationClick = {},
            onFavoriteClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050507L)
@Composable
private fun StationListItemPlayingPreview() {
    RadioThingTheme {
        StationListItem(
            station = previewStation().copy(name = "Soma FM Drone Zone", votes = 10432),
            isPlaying = true,
            onStationClick = {},
            onFavoriteClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050507L)
@Composable
private fun StationListItemFavoritedPreview() {
    RadioThingTheme {
        StationListItem(
            station = previewStation().copy(isFavorite = true),
            isPlaying = false,
            onStationClick = {},
            onFavoriteClick = {}
        )
    }
}

private fun previewStation() = RadioStation(
    stationUuid = "preview-uuid",
    name = "Radio Paradise",
    url = "https://example.com/stream",
    urlResolved = "https://example.com/stream",
    homepage = "",
    favicon = "",
    tags = "eclectic, rock",
    country = "Germany",
    countryCode = "DE",
    language = "english",
    codec = "MP3",
    bitrate = 128,
    votes = 5231,
    clickCount = 100,
    clickTrend = 2,
    lastCheckOk = true
)

// endregion
