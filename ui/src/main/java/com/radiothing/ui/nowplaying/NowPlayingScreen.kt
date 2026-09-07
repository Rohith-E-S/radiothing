package com.radiothing.ui.nowplaying

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.radiothing.ui.components.DotMatrixIcon
import com.radiothing.ui.components.IconType
import com.radiothing.ui.components.countryCodeToEmoji
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Hairline
import com.radiothing.ui.theme.Ink
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.PureBlack
import com.radiothing.ui.theme.TextWhite35
import com.radiothing.ui.theme.DotMatrix
import com.radiothing.domain.model.RadioStation
import com.radiothing.ui.theme.RadioThingTheme

// ── BLACK LAB / OSCILLOSCOPE BENCH ──
// Impeccable surface: whole surface inside BLACK LAB world, composition = Oscilloscope Lab Bench (dealt 4 lead, seed 2426ab76)
// Thesis: radio as live specimen under the scope — CRT is the hero, not a card. Refuses stacked-card feed.
// Pill everywhere (100dp), DotMatrix, red = live only, grid = 1dp hairline.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    viewModel: NowPlayingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NowPlayingContent(
        uiState = uiState,
        spectrumBins = viewModel.spectrumBins,
        playlists = viewModel.playlists.collectAsStateWithLifecycle().value,
        playlistCounts = viewModel.playlistCounts.collectAsStateWithLifecycle().value,
        onBack = onBack,
        onTogglePlayPause = viewModel::togglePlayPause,
        onPrevious = viewModel::previous,
        onNext = viewModel::next,
        onSetVolume = viewModel::setVolume,
        onToggleFavorite = viewModel::toggleFavorite,
        onSeekInQueue = viewModel::seekInQueue,
        onStartSleepTimer = viewModel::startSleepTimer,
        onCancelSleepTimer = viewModel::cancelSleepTimer,
        onAddToPlaylist = viewModel::addStationToPlaylist,
        onCreateAndAdd = viewModel::createPlaylistAndAddStation
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingContent(
    uiState: NowPlayingUiState,
    spectrumBins: kotlinx.coroutines.flow.StateFlow<FloatArray?>,
    playlists: List<com.radiothing.domain.model.Playlist>,
    playlistCounts: Map<Long, Int>,
    onBack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSetVolume: (Float) -> Unit,
    onToggleFavorite: () -> Unit,
    onSeekInQueue: (Int) -> Unit,
    onStartSleepTimer: (Long) -> Unit,
    onCancelSleepTimer: () -> Unit,
    onAddToPlaylist: (Long) -> Unit,
    onCreateAndAdd: (String) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var lastHapticBlock by remember { mutableIntStateOf(-1) }
    var showQueue by remember { mutableStateOf(false) }
    var showSleep by remember { mutableStateOf(false) }
    var showAddToPlaylist by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .windowInsetsPadding(WindowInsets.statusBars)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {

        // ── Header — 48dp, pill-aware, no hairline divider (scope carries chrome) back, Live
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val inf = rememberInfiniteTransition(label = "live")
                    val a by inf.animateFloat(0.35f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "a")
                    Box(Modifier.size(6.dp).clip(CircleShape).background(if (uiState.isPlaying) BrightRed.copy(alpha = a) else Color(0xFF333333)))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        when {
                            uiState.isBuffering -> "TUNING"
                            uiState.isPlaying -> "LIVE"
                            uiState.currentStation == null -> "IDLE"
                            else -> "ARMED"
                        },
                        color = if (uiState.isPlaying) BrightRed else TextWhite35,
                        fontFamily = DotMatrix, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                    )
                }
            }
            // Right counterweight mirroring the 48dp back button — without it
            // the weight(1f) status column centered ~24dp right of screen center
            Spacer(Modifier.size(48.dp))
        }

        if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(100.dp))
//                    .background(Color(0xFF1A0A0A))
                    .border(1.dp, BrightRed.copy(0.5f), RoundedCornerShape(100.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(uiState.error ?: "", color = BrightRed, fontFamily = DotMatrix, fontSize = 11.sp, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    TextButton(onClick = { onTogglePlayPause() }) { Text("RETRY", color = BrightRed, fontFamily = DotMatrix, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(Modifier.height(5.dp))
        }


        // ── Dossier — station name as scope label, pill enclosure
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {

                    Spacer(Modifier.height(0.dp))
                    Text(
                        text = uiState.currentStation?.name?.uppercase() ?: "NO SPECIMEN",
                        color = Color.White, fontFamily = DotMatrix, fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = 0.8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.currentStation?.country?.isNotEmpty() == true) {
                            Text(
                                uiState.currentStation!!.country.uppercase(),
                                color = TextWhite35, fontFamily = DotMatrix, fontSize = 11.sp, letterSpacing = 0.8.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                                // shrink first — votes/tray stay pinned at natural width
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                        Spacer(Modifier.width(2.dp))
                        if ((uiState.currentStation?.votes ?: 0) > 0) {
                            Text("♥ ${uiState.currentStation!!.votes}", color = BrightRed, fontFamily = DotMatrix, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Tags — horizontal pill row, scroll not needed (take 4)
        val tags = uiState.currentStation?.tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.take(4) ?: emptyList()
        if (tags.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Ink)
                            .border(1.dp, GridLine, RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(tag.uppercase(), color = TextWhite35, fontFamily = DotMatrix, fontSize = 9.sp, letterSpacing = 0.6.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Visualizer — hero fills flex so no bottom wasteland; grid + real equalizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, GridLine, RoundedCornerShape(20.dp))
                .padding(10.dp)
        ) {
            // CRT tube
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF050507))
            ) {
                // Equalizer — REAL FFT when available, dotted bars in RED+WHITE like reference image
                StreamDotEqualizer(spectrumBins = spectrumBins, isPlaying = uiState.isPlaying, isBuffering = uiState.isBuffering, modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 8.dp))

                // Station badge — pill, bottom-left inside CRT
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Panel.copy(alpha = 0.92f))
                        .border(1.dp, GridLine, RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Ink)
                                .border(1.dp, Hairline, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val favicon = uiState.currentStation?.favicon ?: ""
                            if (favicon.isNotEmpty()) {
                                AsyncImage(model = favicon, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                            } else {
                                Text(uiState.currentStation?.name?.take(2)?.uppercase() ?: "—", color = Color.White, fontFamily = DotMatrix, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                (uiState.currentStation?.bitrate?.takeIf { it > 0 }?.let { "${it}K" } ?: "LIVE") + (uiState.currentStation?.codec?.takeIf { it.isNotEmpty() }?.let { " • ${it.uppercase()}" } ?: ""),
                                color = TextWhite35, fontFamily = DotMatrix, fontSize = 9.sp, letterSpacing = 0.8.sp
                            )
                            if (uiState.currentStation?.countryCode?.length == 2) {
                                Text("${countryCodeToEmoji(uiState.currentStation!!.countryCode)} ${uiState.currentStation!!.countryCode.uppercase()}", color = Color.White, fontFamily = DotMatrix, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Sleep countdown — top-right pill inside CRT
                if (uiState.sleepRemainingMs > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(BrightRed)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("${uiState.sleepRemainingMs / 60000}:${String.format("%02d", (uiState.sleepRemainingMs % 60000) / 1000)}", color = Color.White, fontFamily = DotMatrix, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }

                if (uiState.isBuffering) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.45f)), contentAlignment = Alignment.Center) {
                        val inf = rememberInfiniteTransition(label = "buf")
                        val d by inf.animateValue(0, 3, Int.VectorConverter, infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Restart), label = "d")
                        Text("●".repeat(d + 1), color = BrightRed, fontFamily = DotMatrix, fontSize = 14.sp)
                    }
                }
            }
        }





        Spacer(Modifier.height(24.dp))

        // ── Transport — primary pill, clear hierarchy: Prev / PLAY / Next only
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10000.dp))
                .background(Panel)
                .padding(horizontal = 4.dp, vertical = 70.dp)
                .semantics { contentDescription = "Transport controls" }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prev — 56dp, labelled, disabled state muted
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(enabled = uiState.queue.size > 1) { onPrevious() }
                        .semantics { contentDescription = "Previous station" }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    DotMatrixIcon(type = IconType.PREV, size = 26.dp, color = if (uiState.queue.size > 1) Color.White else Color(0xFF555555))
                   Spacer(Modifier.height(2.dp))
                }
                // Play — 72dp hero, red, single focal point
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(BrightRed)
                        .clickable { onTogglePlayPause() }
                        .semantics { contentDescription = if (uiState.isPlaying) "Pause" else "Play" },
                    contentAlignment = Alignment.Center
                ) {
                    DotMatrixIcon(type = if (uiState.isPlaying) IconType.PAUSE else IconType.PLAY, size = 32.dp, color = Color.White)
                }
                // Next
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(enabled = uiState.queue.size > 1) { onNext() }
                        .semantics { contentDescription = "Next station" }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    DotMatrixIcon(type = IconType.NEXT, size = 26.dp, color = if (uiState.queue.size > 1) Color.White else Color(0xFF555555))
                    Spacer(Modifier.height(2.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Volume — single pill: blocks ARE the slider (tap or slide anywhere on the track)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(100.dp))
                .border(1.dp, GridLine, RoundedCornerShape(100.dp))
                .padding(horizontal = 14.dp, vertical = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("VOL", color = TextWhite35, fontFamily = DotMatrix, fontSize = 9.sp, letterSpacing = 1.sp)
                Spacer(Modifier.width(10.dp))
                val blocks = 10
                val filled = (uiState.volume * blocks).toInt()
                // Gesture lives on a 44dp+ wrapper; the 14dp visual track alone
                // was far below the touch minimum and taps near it missed
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .semantics {
                            contentDescription = "Volume"
                            stateDescription = "${(uiState.volume * 100).toInt()} percent"
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                val block = (fraction * blocks).toInt()
                                if (block != lastHapticBlock) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    lastHapticBlock = block
                                }
                                onSetVolume(fraction)
                            }
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, _ ->
                                val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                val block = (fraction * blocks).toInt()
                                if (block != lastHapticBlock) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    lastHapticBlock = block
                                }
                                onSetVolume(fraction)
                                change.consume()
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.fillMaxWidth().height(14.dp)
                    ) {
                        repeat(blocks) { idx ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (idx < filled) BrightRed else Color(0xFF1A1A1E))
                            )
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
            }
        }
        Spacer(Modifier.height(24.dp))



        // ── Bottom bar — secondary pill, distinct from transport, labelled, not crowded
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(100.dp))
                .background(Panel)
                .border(1.dp, GridLine, RoundedCornerShape(100.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Equal quarters — weight(1f) per section so the dividers split the bar
            // proportionally regardless of label width (QUEUE n/m must not reflow the rest)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //favourite
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    UtilityChip(
                        icon = if (uiState.currentStation?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        label = "FAV",
                        active = uiState.currentStation?.isFavorite == true,
                        onClick = { onToggleFavorite() }
                    )
                }
                Box(Modifier.width(1.dp).height(36.dp).background(GridLine.copy(alpha = 0.6f)))
                //Playlist
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    UtilityChip(icon = Icons.AutoMirrored.Filled.PlaylistAdd, label = "TRAY+", active = false, onClick = { showAddToPlaylist = true })
                }
                Box(Modifier.width(1.dp).height(36.dp).background(GridLine.copy(alpha = 0.6f)))

                //Sleep
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    UtilityChip(icon = Icons.Default.Timer, label = if (uiState.sleepRemainingMs > 0) "${uiState.sleepRemainingMs / 60000}M" else "SLEEP", active = uiState.sleepRemainingMs > 0, onClick = { showSleep = true })
                }
                Box(Modifier.width(1.dp).height(36.dp).background(GridLine.copy(alpha = 0.6f)))

                //Share
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    UtilityChip(icon = Icons.Default.Share, label = "SHARE", onClick = {
                        uiState.currentStation?.let { s ->
                            val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "${s.name} - ${s.urlResolved.ifEmpty { s.url }}") }
                            // No chooser handler is legal on some devices/AOSP builds
                            runCatching { context.startActivity(Intent.createChooser(send, "Share")) }
                        }
                    })
                }
                Box(Modifier.width(1.dp).height(36.dp).background(GridLine.copy(alpha = 0.6f)))

                //Queue
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    UtilityChip(icon = Icons.AutoMirrored.Filled.QueueMusic, label = "QUEUE${if (uiState.queue.size > 1) " ${uiState.queueIndex + 1}/${uiState.queue.size}" else ""}", onClick = { showQueue = true })
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }

    // ── Queue sheet — board style, pill rows
    if (showQueue) {
        ModalBottomSheet(
            onDismissRequest = { showQueue = false },
            containerColor = PureBlack,
            contentColor = Color.White,
            scrimColor = PureBlack.copy(alpha = 0.75f),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("TRAY", color = Color.White, fontFamily = DotMatrix, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.5.sp)
                    Text("${uiState.queue.size} SPECIMENS", color = TextWhite35, fontFamily = DotMatrix, fontSize = 10.sp)
                }
                Spacer(Modifier.height(12.dp))
                if (uiState.queue.isEmpty()) {
                    Text("TRAY EMPTY — TUNE FROM BROWSE", color = TextWhite35, fontFamily = DotMatrix, fontSize = 11.sp)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 360.dp)) {
                        itemsIndexed(uiState.queue, key = { i, _ -> i }) { idx, station ->
                            val isCurrent = idx == uiState.queueIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (isCurrent) Panel else Ink)
                                    .border(1.dp, if (isCurrent) BrightRed.copy(0.4f) else GridLine, RoundedCornerShape(100.dp))
                                    .clickable { onSeekInQueue(idx); showQueue = false }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(String.format("%02d", idx + 1), color = if (isCurrent) BrightRed else TextWhite35, fontFamily = DotMatrix, fontSize = 10.sp, modifier = Modifier.width(28.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(station.name.uppercase(), color = if (isCurrent) Color.White else Color(0xFFCCCCCC), fontFamily = DotMatrix, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${station.codec.uppercase().takeIf { it.isNotEmpty() } ?: "LIVE"} • ${station.bitrate.takeIf { it > 0 }?.let { "${it}K" } ?: ""}", color = TextWhite35, fontFamily = DotMatrix, fontSize = 9.sp, maxLines = 1)
                                }
                                if (isCurrent) Box(Modifier.size(6.dp).clip(CircleShape).background(BrightRed))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    if (showAddToPlaylist) {
        val station = uiState.currentStation
        if (station != null) {
            com.radiothing.ui.components.AddToPlaylistSheet(
                station = station,
                playlists = playlists,
                counts = playlistCounts,
                onAddTo = { id ->
                    onAddToPlaylist(id)
                    showAddToPlaylist = false
                },
                onCreateAndAdd = { name ->
                    onCreateAndAdd(name)
                    showAddToPlaylist = false
                },
                onDismiss = { showAddToPlaylist = false }
            )
        } else {
            showAddToPlaylist = false
        }
    }

    if (showSleep) {
        ModalBottomSheet(
            onDismissRequest = { showSleep = false },
            containerColor = PureBlack,
            contentColor = Color.White,
            scrimColor = PureBlack.copy(alpha = 0.75f),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SLEEP", color = Color.White, fontFamily = DotMatrix, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.5.sp)
                Text("Scope dims and tray stops", color = TextWhite35, fontFamily = DotMatrix, fontSize = 11.sp)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(5, 15, 30, 60).forEach { mins ->
                        OutlinedButton(
                            onClick = { onStartSleepTimer(mins * 60_000L); showSleep = false },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GridLine)
                        ) { Text("${mins}M", fontFamily = DotMatrix, fontSize = 11.sp) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onCancelSleepTimer(); showSleep = false },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightRed)
                ) { Text("CANCEL", fontFamily = DotMatrix, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}



@Composable
private fun UtilityChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, active: Boolean = false, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = if (active) BrightRed else TextWhite35, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, color = if (active) BrightRed else TextWhite35, fontFamily = DotMatrix, fontSize = 8.sp, letterSpacing = 0.8.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}


// ── Previews — static sample data; callbacks are no-ops, audio session 0 = synthetic waveform
@Preview(showBackground = true, backgroundColor = 0xFF000000L, device = "spec:width=412dp,height=915dp", name = "Playing")
@Preview(showBackground = true, backgroundColor = 0xFF000000L, device = "spec:width=412dp,height=915dp", name = "Queue in tray")
@Composable
private fun NowPlayingScreenPreview() {
    val specimen = RadioStation(
        stationUuid = "preview-1", name = "SomaFM Groove Salad", url = "", urlResolved = "",
        homepage = "", favicon = "", tags = "ambient,chill,electronic,downtempo",
        country = "United States", countryCode = "US", language = "english",
        codec = "AAC", bitrate = 128, votes = 312, clickCount = 9200, clickTrend = 0, lastCheckOk = true
    )
    val specimen2 = specimen.copy(stationUuid = "preview-2", name = "NTS Radio 1", codec = "MP3", bitrate = 320)
    RadioThingTheme {
        NowPlayingContent(
            uiState = NowPlayingUiState(
                currentStation = specimen,
                isPlaying = true,
                volume = 0.6f,
                queue = listOf(specimen, specimen2),
                queueIndex = 0
            ),
            spectrumBins = kotlinx.coroutines.flow.MutableStateFlow(null),
            playlists = emptyList(),
            playlistCounts = emptyMap(),
            onBack = {}, onTogglePlayPause = {}, onPrevious = {}, onNext = {},
            onSetVolume = {}, onToggleFavorite = {}, onSeekInQueue = {},
            onStartSleepTimer = {}, onCancelSleepTimer = {},
            onAddToPlaylist = {}, onCreateAndAdd = {}
        )
    }
}

