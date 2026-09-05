package com.radiothing.ui.browse

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.model.StationQuery
import com.radiothing.domain.repository.FavoriteRepository
import com.radiothing.domain.repository.StationRepository
import com.radiothing.domain.usecase.GetTopStationsUseCase
import com.radiothing.domain.usecase.SearchStationsUseCase
import com.radiothing.domain.usecase.ToggleFavoriteUseCase
import com.radiothing.player.PlayerManager
import com.radiothing.ui.components.FilterSelection
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val getTopStationsUseCase: GetTopStationsUseCase,
    private val searchStationsUseCase: SearchStationsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val playerManager: PlayerManager,
    private val favoriteRepository: FavoriteRepository,
    private val stationRepository: StationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BrowseUiState())
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    // Favicon cache policy (intuitive mode):
    // 1. First time a station appears → row shows flag/initials placeholder only.
    // 2. VM silently prefetches its favicon into Coil memory+disk cache.
    // 3. Once cached, uuid lands in iconReady → row swaps to the icon instantly.
    //    Scrolling back / re-entering viewport = pure memory-cache hit, zero decode jank.
    // Set grows monotonically for the session — filter changes keep icons visible
    // (no flicker), a new search simply adds new stations that start as flags.
    private val _iconReady = MutableStateFlow<Set<String>>(emptySet())
    val iconReady: StateFlow<Set<String>> = _iconReady.asStateFlow()
    private val queuedIconFetches = mutableSetOf<String>()

    // Server catalogs for the filter sheet — loaded once, cached for the session
    private val _countries = MutableStateFlow<List<String>>(emptyList())
    val countries: StateFlow<List<String>> = _countries.asStateFlow()
    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()
    private val _languages = MutableStateFlow<List<String>>(emptyList())
    val languages: StateFlow<List<String>> = _languages.asStateFlow()

    companion object {
        private const val PAGE_SIZE = 20
        const val THUMB_PX = 112
    }

    private var lastRequestedOffset = -1

    /**
     * Monotonic token bumped on every search/filter change. In-flight
     * `loadMore` calls capture the current token; before merging their
     * results, they check the token is still current and discard if the
     * query changed under them. Without this, a filter change mid-scroll
     * could append a page from the previous order to the new result set.
     */
    private var searchToken = 0L

    /** Build the server query for the current state (filters + search text). */
    private fun currentServerQuery(state: BrowseUiState, offset: Int, limit: Int): StationQuery {
        val parsed = searchStationsUseCase.parseQuery(state.searchQuery)
        return parsed.copy(
            country = state.filters.country ?: parsed.country,
            tag = state.filters.tag ?: parsed.tag,
            language = state.filters.language ?: parsed.language,
            name = parsed.name,
            order = state.filters.order,
            offset = offset,
            limit = limit
        )
    }

    /** True when the query terms should hit the search endpoint. */
    private fun hasServerTerms(state: BrowseUiState): Boolean {
        val parsed = searchStationsUseCase.parseQuery(state.searchQuery)
        val hasParsedTerms = parsed.name != null || parsed.tag != null || parsed.country != null || parsed.language != null
        return state.filters.hasServerFilters || hasParsedTerms
    }

    /**
     * Warm Coil memory+disk cache for station favicons in the background.
     * Keys MUST match the display request in StationListItem ("st_${uuid}", size 112)
     * so the row's AsyncImage hits memory cache instantly when iconReady flips.
     * Batched updates: coalesce many onSuccess callbacks into one set update
     * to avoid 20 recompositions in one frame at 120Hz.
     */
    private val pendingIconReady = mutableSetOf<String>()
    private var iconBatchJob: kotlinx.coroutines.Job? = null

    private fun prefetchIcons(stations: List<RadioStation>) {
        for (station in stations) {
            if (station.favicon.isEmpty()) continue
            if (!_iconReady.value.contains(station.stationUuid) && queuedIconFetches.add(station.stationUuid)) {
                val request = ImageRequest.Builder(appContext)
                    .data(station.favicon)
                    .size(THUMB_PX)
                    .scale(coil.size.Scale.FILL)
                    .crossfade(false)
                    .allowHardware(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCacheKey("st_${station.stationUuid}")
                    .diskCacheKey("st_${station.stationUuid}")
                    // Decode fully in background (IO) before marking ready — guarantees
                    // AsyncImage in StationListItem hits memory cache synchronously, no jank on scroll.
                    .listener(
                        onSuccess = { _, result ->
                            // result is already decoded on background thread; batch mark ready
                            pendingIconReady.add(station.stationUuid)
                            if (iconBatchJob?.isActive != true) {
                                iconBatchJob = viewModelScope.launch {
                                    kotlinx.coroutines.delay(80)
                                    val batch = pendingIconReady.toSet().also { pendingIconReady.clear() }
                                    _iconReady.update { it + batch }
                                }
                            }
                        }
                        // onError: leave out of iconReady → row keeps flag placeholder
                    )
                    .build()
                // Enqueue with high priority — decode on Coil's background dispatcher, not Main.
                // Using enqueue (not execute) keeps UI thread free; decoded bitmap lands in memory cache.
                appContext.imageLoader.enqueue(request)
            }
        }
    }

    init {
        loadTopStations()
        loadCatalogs()
        // Keep isFavorite in sync if user favorites from NowPlaying/player
        // distinctUntilChanged → no redundant list rebuilds mid-scroll
        // Mapping is offloaded to Default so it never blocks the 8ms frame budget at 120Hz
        viewModelScope.launch {
            favoriteRepository.getFavoriteIds()
                .distinctUntilChanged()
                .collect { ids ->
                    val snapshot = _uiState.value.unfilteredStations
                    if (snapshot.isEmpty()) return@collect
                    val enrichedUnfiltered = withContext(Dispatchers.Default) {
                        snapshot.map { it.copy(isFavorite = ids.contains(it.stationUuid)) }
                    }
                    _uiState.value = _uiState.value.copy(unfilteredStations = enrichedUnfiltered)
                    applyFiltersInternal()
                }
        }
    }

    private fun loadCatalogs() {
        viewModelScope.launch {
            try {
                stationRepository.getCountries().getOrNull()
                    ?.sortedByDescending { it.stationCount }
                    ?.map { it.name }
                    ?.let { _countries.value = it }
            } catch (e: Exception) {
                android.util.Log.w("BrowseViewModel", "loadCatalogs: countries failed", e)
            }
        }
        viewModelScope.launch {
            try {
                stationRepository.getGenres().getOrNull()
                    ?.sortedByDescending { it.stationCount }
                    ?.map { it.name }
                    ?.let { _tags.value = it }
            } catch (e: Exception) {
                android.util.Log.w("BrowseViewModel", "loadCatalogs: genres failed", e)
            }
        }
        viewModelScope.launch {
            try {
                stationRepository.getLanguages().getOrNull()
                    ?.sortedByDescending { it.stationCount }
                    ?.map { it.name }
                    ?.let { _languages.value = it }
            } catch (e: Exception) {
                android.util.Log.w("BrowseViewModel", "loadCatalogs: languages failed", e)
            }
        }
    }

    private suspend fun enrichWithFavorites(stations: List<RadioStation>): List<RadioStation> {
        return try {
            val ids = favoriteRepository.getFavoriteIds().first()
            withContext(Dispatchers.Default) {
                stations.map { it.copy(isFavorite = ids.contains(it.stationUuid)) }
            }
        } catch (_: Exception) { stations }
    }

    private fun loadPage(refresh: Boolean, state: BrowseUiState, offset: Int) {
        viewModelScope.launch {
            if (refresh) _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            else _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val stationsResult = if (hasServerTerms(state)) {
                    searchStationsUseCase(currentServerQuery(state, offset, PAGE_SIZE))
                } else {
                    getTopStationsUseCase.byOrder(state.filters.order, offset = offset, limit = PAGE_SIZE)
                }
                val stations = enrichWithFavorites(stationsResult.getOrElse { throw it })
                if (offset == 0) lastRequestedOffset = -1
                _uiState.value = _uiState.value.copy(
                    unfilteredStations = stations, isLoading = false, isRefreshing = false, hasSearched = hasServerTerms(state),
                    canLoadMore = stations.size >= PAGE_SIZE, currentOffset = stations.size, error = null, loadMoreError = null
                )
                prefetchIcons(stations)
                applyFiltersInternal()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = friendlyError(e))
            }
        }
    }

    private fun loadTopStations(refresh: Boolean = false) {
        val state = _uiState.value
        loadPage(refresh, state, offset = 0)
    }

    fun refresh() {
        // Invalidate in-flight load-more: without a token bump its merge passes
        // the staleness check and can append onto the fresh first page, leaving
        // currentOffset pointing past a skipped window of the new ordering.
        searchToken++
        loadTopStations(refresh = true)
    }

    fun retry() {
        searchToken++
        loadTopStations(refresh = true)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun performSearch(refresh: Boolean = false) {
        searchToken++
        loadTopStations(refresh = refresh)
    }

    fun applyFilters(selection: FilterSelection) {
        searchToken++
        _uiState.value = _uiState.value.copy(filters = selection)
        applyFiltersInternal()
        // Server-side filters change the result set — refetch
        loadTopStations(refresh = true)
    }

    fun clearFilters() {
        searchToken++
        _uiState.value = _uiState.value.copy(filters = FilterSelection())
        applyFiltersInternal()
        loadTopStations(refresh = true)
    }

    private fun applyFiltersInternal() {
        // Snapshot to avoid race with concurrent updates; filtering off Main prevents 120Hz jank
        val snapshot = _uiState.value
        if (snapshot.filters.bitrates.isEmpty() && snapshot.filters.codecs.isEmpty()) {
            // Fast path: no filter → avoid Default hop, but still avoid blocking frame on large copy
            _uiState.value = snapshot.copy(stations = snapshot.unfilteredStations)
            return
        }
        viewModelScope.launch(Dispatchers.Default) {
            var list: List<RadioStation> = snapshot.unfilteredStations
            if (snapshot.filters.bitrates.isNotEmpty()) {
                list = list.filter { station -> snapshot.filters.bitrates.contains(station.bitrate.toString()) }
            }
            if (snapshot.filters.codecs.isNotEmpty()) {
                val lower = snapshot.filters.codecs.map { it.lowercase() }.toSet()
                list = list.filter { lower.contains(it.codec.lowercase()) }
            }
            _uiState.value = _uiState.value.copy(stations = list)
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (!state.canLoadMore || state.isLoadingMore || state.isLoading) return
        val nextOffset = state.currentOffset
        // guard: this page was already requested (effect can re-fire on recomposition)
        if (nextOffset == lastRequestedOffset) return
        lastRequestedOffset = nextOffset
        val capturedToken = searchToken
        _uiState.value = state.copy(isLoadingMore = true)
        viewModelScope.launch {
            try {
                val result = if (state.hasSearched) {
                    searchStationsUseCase(currentServerQuery(state, nextOffset, PAGE_SIZE))
                } else {
                    getTopStationsUseCase.byOrder(state.filters.order, offset = nextOffset, limit = PAGE_SIZE)
                }
                val fresh = enrichWithFavorites(result.getOrElse { throw it })
                val existing = _uiState.value.unfilteredStations
                // dedupe by uuid — offload O(n) work from Main (n grows to hundreds, would blow 8ms budget)
                val (merged, added, canLoadMore) = withContext(Dispatchers.Default) {
                    val seen = existing.map { it.stationUuid }.toSet()
                    val newOnes = fresh.filter { !seen.contains(it.stationUuid) }
                    Triple(existing + newOnes, newOnes, newOnes.isNotEmpty() && fresh.size >= PAGE_SIZE)
                }
                // Discard stale results — the user changed the query while we were
                // fetching, and the result set no longer matches what's on screen.
                if (searchToken != capturedToken) return@launch
                _uiState.value = _uiState.value.copy(
                    unfilteredStations = merged,
                    isLoadingMore = false,
                    currentOffset = merged.size,
                    canLoadMore = canLoadMore,
                    loadMoreError = null
                )
                prefetchIcons(added)
                applyFiltersInternal()
            } catch (e: Exception) {
                if (searchToken != capturedToken) return@launch
                // Surface inline (the loaded list stays visible) and keep
                // lastRequestedOffset blocking auto-refetch — a scroll parked at
                // the list end would otherwise retry in a tight loop. The banner's
                // retry button calls retryLoadMore() explicitly.
                _uiState.value = _uiState.value.copy(isLoadingMore = false, loadMoreError = friendlyError(e))
            }
        }
    }

    /** User-driven retry after a failed load-more (see loadMoreError). */
    fun retryLoadMore() {
        lastRequestedOffset = -1
        loadMore()
    }


    fun playStation(stationUuid: String) {
        val station = _uiState.value.stations.find { it.stationUuid == stationUuid }
        if (station != null) {
            playerManager.play(station, _uiState.value.stations)
            // Feed the community catalog — play counts are how Radio Browser ranks
            viewModelScope.launch { try { stationRepository.clickStation(stationUuid) } catch (_: Exception) {} }
        }
    }

    fun toggleFavorite(station: RadioStation) {
        viewModelScope.launch {
            toggleFavoriteUseCase(station)
            // Immediate flip — map off Main so tap doesn't jank scroll at 120Hz
            val snapshot = _uiState.value.unfilteredStations
            val newFav = !station.isFavorite
            val updatedUnfiltered = withContext(Dispatchers.Default) {
                snapshot.map { if (it.stationUuid == station.stationUuid) it.copy(isFavorite = newFav) else it }
            }
            _uiState.value = _uiState.value.copy(unfilteredStations = updatedUnfiltered)
            applyFiltersInternal()
        }
    }

    private fun friendlyError(e: Throwable): String =
        com.radiothing.player.StreamErrorMessages.from(e)
}

data class BrowseUiState(
    val searchQuery: String = "",
    val stations: List<RadioStation> = emptyList(),
    val unfilteredStations: List<RadioStation> = emptyList(),
    val filters: FilterSelection = FilterSelection(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    /** Failed load-more — shown inline; the loaded list stays visible. */
    val loadMoreError: String? = null,
    val hasSearched: Boolean = false,
    val canLoadMore: Boolean = true,
    val currentOffset: Int = 0
)
