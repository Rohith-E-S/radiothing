package com.radiothing.ui.browse

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.State
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.model.PlayerState
import com.radiothing.player.PlayerManager
import com.radiothing.ui.common.EmptyState
import com.radiothing.ui.common.EmptyStateType
import com.radiothing.ui.common.ErrorState
import com.radiothing.ui.components.DotMatrixIcon
import com.radiothing.ui.components.FilterSelection
import com.radiothing.ui.components.FilterSheet
import com.radiothing.ui.components.IconType
import com.radiothing.ui.components.NothingTextField
import com.radiothing.ui.components.StationListItem
import com.radiothing.ui.components.StationListSkeleton
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Hairline
import com.radiothing.ui.theme.Ink
import com.radiothing.ui.theme.Ndot57
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.TextWhite35
import com.radiothing.ui.theme.TextWhite70
import com.radiothing.ui.common.LocalBottomClearance
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel,
    playerManager: PlayerManager,
    onStationClick: (String) -> Unit
) {
    // Split collection — header/search/list only recompose when their slice changes
    // Prevents LazyColumn full recomposition on every keystroke (searchQuery) at 120Hz
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Keep State wrapper — items use derivedStateOf to read membership, so only
    // the item whose boolean flips recomposes (not all N items per icon load).
    val iconReadyState = viewModel.iconReady.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    var showFilters by remember { mutableStateOf(false) }

    // Keep State wrapper — same derivedStateOf trick as iconReady.
    val playerStateState = playerManager.playerState.collectAsStateWithLifecycle()

    // Pull-to-refresh kept but will be attached only when list is idle at top (see BrowseStationList)
    // — avoids nestedScroll dispatch on every scroll frame at 120Hz. Trades instant pull sensitivity for butter.
    val pullState = rememberPullToRefreshState()
    val canPullRefresh = !uiState.isLoading && !uiState.isLoadingMore

    // Gesture → one refresh; end as soon as VM's isRefreshing resolves
    if (pullState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refresh()
        }
    }
    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing && pullState.isRefreshing) {
            pullState.endRefresh()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        BrowseHeader(stationCount = uiState.stations.size)

        Spacer(Modifier.height(6.dp))

        BrowseSearchRow(
            query = uiState.searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            onSearch = {
                viewModel.performSearch()
                keyboardController?.hide()
            },
            onFilterClick = { showFilters = true }
        )

        BrowseFilterHints(
            query = uiState.searchQuery,
            onClearQuery = { viewModel.onSearchQueryChange(""); viewModel.performSearch() }
        )

        BrowseActiveFilters(
            filters = uiState.filters,
            onClear = { viewModel.clearFilters() }
        )

            Box(modifier = Modifier.fillMaxSize().padding(bottom = 0.dp)) {
            when {
                uiState.isLoading -> {
                    StationListSkeleton(modifier = Modifier.fillMaxSize().padding(top = 4.dp))
                }
                uiState.error != null -> {
                    ErrorState(message = uiState.error ?: "Unknown error", onRetry = { viewModel.retry() })
                }
                uiState.stations.isEmpty() -> {
                    EmptyState(type = EmptyStateType.NO_RESULTS, modifier = Modifier.fillMaxSize().padding(bottom = LocalBottomClearance.current))
                }
                else -> {
                    Column {
                        if (uiState.loadMoreError != null) {
                            LoadMoreErrorBanner(
                                message = uiState.loadMoreError!!,
                                onRetry = { viewModel.retryLoadMore() }
                            )
                        }
                        BrowseStationList(
                            stations = uiState.stations,
                            canLoadMore = uiState.canLoadMore,
                            isLoadingMore = uiState.isLoadingMore,
                            isLoading = uiState.isLoading,
                            iconReadyState = iconReadyState,
                            playerStateState = playerStateState,
                            onLoadMore = viewModel::loadMore,
                            onStationClick = { uuid ->
                                viewModel.playStation(uuid)
                                onStationClick(uuid)
                            },
                            onFavoriteClick = { viewModel.toggleFavorite(it) },
                            pullState = pullState,
                            canPullRefresh = canPullRefresh
                        )
                    }
                }
            }
        }
    }

    if (showFilters) {
        FilterSheet(
            onApply = { selection: FilterSelection ->
                viewModel.applyFilters(selection)
                showFilters = false
            },
            onClear = {
                viewModel.clearFilters()
                showFilters = false
            },
            onDismiss = { showFilters = false },
            initial = uiState.filters,
            countries = viewModel.countries.collectAsStateWithLifecycle().value,
            tags = viewModel.tags.collectAsStateWithLifecycle().value,
            languages = viewModel.languages.collectAsStateWithLifecycle().value
        )
    }
}

@Composable
private fun BrowseHeader(stationCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "BROWSE",
                color = Color.White,
                fontFamily = Ndot57,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = 2.sp,
                lineHeight = 20.sp
            )
            Spacer(Modifier.width(10.dp))
        }
        if (stationCount > 0) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$stationCount",
                    color = BrightRed,
                    fontFamily = Ndot57,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 0.8.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "AVAILABLE",
                    color = TextWhite35,
                    fontFamily = Ndot57,
                    fontSize = 8.sp,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun BrowseSearchRow(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NothingTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "SEARCH",
            onSearch = onSearch,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(Panel)
                .border(1.dp, Hairline, RoundedCornerShape(100.dp))
                .semantics { contentDescription = "Filters" }
        ) {
            // ASCII-art funnel in the app's dot-matrix language (matches nav glyphs)
            DotMatrixIcon(type = IconType.FILTER, size = 18.dp, color = Color.White)
        }
    }
}

@Composable
private fun BrowseFilterHints(query: String, onClearQuery: () -> Unit) {
    // Animated in/out: a hard if/else made the whole list jump ~24dp the
    // moment the first character was typed (hint row appeared / spacer shrank).
    AnimatedVisibility(
        visible = query.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "QUERY “${query.uppercase().take(24)}”",
                color = TextWhite35,
                fontFamily = Ndot57,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
            )
            TextButton(
                onClick = onClearQuery,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("CLEAR", color = BrightRed, fontFamily = Ndot57, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BrowseActiveFilters(
    filters: FilterSelection,
    onClear: () -> Unit
) {
    if (filters.hasAnyFilters) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("FILTERS:", color = TextWhite35, fontFamily = Ndot57, fontSize = 9.sp, letterSpacing = 0.8.sp)
            filters.country?.let {
                FilterPill("IN ${it.uppercase()}")
            }
            filters.tag?.let {
                FilterPill("TAG ${it.uppercase()}")
            }
            filters.language?.let {
                FilterPill("LANG ${it.uppercase()}")
            }
            filters.bitrates.forEach { br -> FilterPill("${br}K") }
            filters.codecs.forEach { cd -> FilterPill(cd.uppercase()) }
            if (filters.order != com.radiothing.domain.model.StationOrder.VOTES) {
                FilterPill("BY ${filters.order.label}")
            }
            Text(
                "CLEAR",
                color = BrightRed,
                fontFamily = Ndot57,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .clickable(onClick = onClear)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun FilterPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(BrightRed.copy(alpha = 0.14f))
            .border(1.dp, BrightRed, RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) { Text(text, color = BrightRed, fontFamily = Ndot57, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
}

/** Inline load-more failure — the loaded list stays visible; retry is explicit. */
@Composable
private fun LoadMoreErrorBanner(message: String, onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrightRed.copy(alpha = 0.12f))
            .clickable(onClick = onRetry)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$message — TAP TO RETRY",
            color = BrightRed,
            fontFamily = Ndot57,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowseStationList(
    stations: List<RadioStation>,
    canLoadMore: Boolean,
    isLoadingMore: Boolean,
    isLoading: Boolean,
    iconReadyState: State<Set<String>>,
    playerStateState: State<PlayerState>,
    pullState: PullToRefreshState,
    canPullRefresh: Boolean,
    onLoadMore: () -> Unit,
    onStationClick: (String) -> Unit,
    onFavoriteClick: (RadioStation) -> Unit
) {
    val listState = rememberLazyListState()

    // Throttle pagination trigger to avoid burst on fast 120Hz fling.
    var lastLoadMoreMs by remember { mutableLongStateOf(0L) }
    val throttledLoadMore = remember(onLoadMore) {
        {
            val now = System.currentTimeMillis()
            if (now - lastLoadMoreMs > 300) {
                lastLoadMoreMs = now
                onLoadMore()
            }
        }
    }

    // Pagination — derivedState samples layoutInfo without emitting on every frame.
    // Keyed on the list-state values the predicate reads: an unkeyed remember
    // captures the first composition's values, so load-more could never fire when
    // the first page fit (canLoadMore started false) or fired against a stale total.
    val shouldLoadMore by remember(stations, canLoadMore, isLoadingMore, isLoading) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total = stations.size
            !listState.isScrollInProgress && lastVisible >= 0 && total > 0 && lastVisible >= total - 4 && canLoadMore && !isLoadingMore && !isLoading
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) throttledLoadMore()
    }

    // Gate nestedScroll to top only + idle — no intercept while scrolling/flinging at 120Hz.
    val isAtTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 }
    }
    val isIdleAtTop by remember {
        derivedStateOf { isAtTop && !listState.isScrollInProgress }
    }
    val effectiveCanPull = canPullRefresh && isIdleAtTop
    val nestedScroll = if (effectiveCanPull) {
        Modifier.nestedScroll(pullState.nestedScrollConnection)
    } else Modifier

    Box(modifier = nestedScroll.fillMaxSize()) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = LocalBottomClearance.current, top = 4.dp)
        ) {
            items(
                stations,
                key = { it.stationUuid },
                contentType = { "station" }
            ) { station ->
                // Memoize per-item lambdas — preserves skippability at 120Hz.
                val stationClick = remember(station.stationUuid, onStationClick) { { onStationClick(station.stationUuid) } }
                val favClick = remember(station.stationUuid, onFavoriteClick) { { onFavoriteClick(station) } }

                // derivedStateOf reads the State wrapper; only recomposes THIS item
                // when the boolean result flips (not when any other station changes).
                val isPlaying by remember(station.stationUuid) {
                    derivedStateOf {
                        val ps = playerStateState.value
                        ps.currentStation?.stationUuid == station.stationUuid
                            && ps.isPlaying
                            && !listState.isScrollInProgress
                    }
                }
                val showIcon by remember(station.stationUuid) {
                    derivedStateOf { iconReadyState.value.contains(station.stationUuid) }
                }

                StationListItem(
                    station = station,
                    isPlaying = isPlaying,
                    onStationClick = stationClick,
                    onFavoriteClick = favClick,
                    showIcon = showIcon,
                    compactMode = true
                )
            }
            if (canLoadMore) {
                item(key = "load_more") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = BrightRed)
                            Spacer(Modifier.width(8.dp))
                            Text("TUNING MORE…", color = TextWhite35, fontFamily = Ndot57, fontSize = 10.sp, letterSpacing = 1.sp)
                        }
                    }
                }
            } else if (stations.isNotEmpty()) {
                item(key = "end") {
                    Text(
                        "END OF SPECTRUM — ${stations.size} STATIONS",
                        color = TextWhite35,
                        fontFamily = Ndot57,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }
            }
        }
        // Visible during the whole pull gesture, not just after release —
        // previously the drag gave zero feedback until isRefreshing flipped.
        val pullFraction = if (effectiveCanPull) pullState.progress else 0f
        if (pullState.isRefreshing || pullFraction > 0f) {
            val dragging = !pullState.isRefreshing
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .alpha(if (dragging) (pullFraction * 2f).coerceIn(0f, 1f) else 1f)
                    .graphicsLayer {
                        scaleX = if (dragging) 0.7f + 0.3f * pullFraction else 1f
                        scaleY = if (dragging) 0.7f + 0.3f * pullFraction else 1f
                    }
                    .clip(RoundedCornerShape(100.dp))
                    .background(Ink)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (pullState.isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = BrightRed)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (dragging) "PULL TO TUNE…" else "TUNING…",
                        color = TextWhite70, fontFamily = Ndot57, fontSize = 10.sp, letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
