/*
 * BLACK LAB — direction contract (seed db9cad78 · assigned 7/7 · operate)
 * THESIS: Matte black field instrument, not poster catalog. Red is live-only; air and 1dp chrome carry the form. Refuses streaming-wall rows and terracotta-serif defaults.
 * OWN-WORLD: PureBlack #050507 / Panel #121214 / Grid #232326 / Signal Red #FF3344 + Amber #FFA231; Fragment Mono caps + Geist body; 16dp cards, 1dp hairlines, 52dp art, tonal layering (no shadows).
 * STORY: Explorer/curator/lean-back finds a station in <30s, sees the living pulse (dot + meter + visualizer), saves to collection, and leaves audio running — intent is tuning, not browsing.
 * FIRST VIEWPORT: Browse — instrument header (BROWSE + spectrum subtitle + red pulse + count), search enclosure (Panel + GO pill) + filter 48dp, hairline divider, airy 10dp station enclosures with 52dp artwork; nav is inset Panel pill on Ink.
 * FORM: Air-Gapped Signal Lab (candidate 7) + Braun Transistor (pick) — matte anodized bench, amber seven-seg memory, Rams minimal density; competitive: split-flap (cascade) + one-bit (pixel chrome).
 * RAISES: Art-forward scale · Module-snap · Edge-light · Deployment.
 * FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
 */
package com.radiothing.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.radiothing.ui.components.MiniPlayer
import com.radiothing.ui.navigation.BottomNavBar
import com.radiothing.ui.navigation.RadioNavHost
import com.radiothing.ui.navigation.Screen
import com.radiothing.ui.navigation.TOP_LEVEL_TABS
import com.radiothing.ui.navigation.TabPager
import com.radiothing.ui.common.LocalBottomClearance
import com.radiothing.ui.theme.RadioThingTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK)
        )
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Force high refresh rate when available — trade battery for butter (120Hz)
        try {
            window.attributes = window.attributes.apply {
                preferredRefreshRate = 120f
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.decorView.post {
                    try { window.decorView.requestLayout() } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}

        setContent {
            RadioThingTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val playerState by viewModel.playerState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }
                // Error-event key — increments on every error change, even when the
                // message string is the same as the last one. Without it, repeated
                // identical errors (same message from same source) would not retrigger
                // the snackbar because LaunchedEffect keys on `error` by value.
                var errorKey by remember { mutableIntStateOf(0) }
                var lastErrorMessage by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    // NOTE: RECORD_AUDIO is deliberately NOT requested at cold start.
                    // It's only needed for the Now Playing visualizer and is requested
                    // contextually from StreamVisualizer on first open, with a graceful
                    // fallback to the simulated waveform when denied.
                }

                LaunchedEffect(playerState.error) {
                    val msg = playerState.error
                    if (msg != null && msg != lastErrorMessage) {
                        lastErrorMessage = msg
                        errorKey++
                    } else if (msg == null) {
                        lastErrorMessage = null
                    }
                }
                LaunchedEffect(errorKey) {
                    lastErrorMessage?.let { snackbarHostState.showSnackbar(it) }
                }

                val isNowPlayingScreen = currentRoute == Screen.NowPlaying.route
                val isPlaylistDetail = currentRoute?.let { route ->
                    route.startsWith("playlist/") && route != "playlist/"
                } == true
                val showOverlay = isNowPlayingScreen || isPlaylistDetail
                // Miniplayer for any loaded station — paused included. It's the only
                // entry point to Now Playing and the only visible resume control, so
                // hiding it on pause left the user with no playback affordance at all.
                // It hides only when no station is loaded or an overlay screen covers it.
                val showMiniPlayer = !showOverlay && playerState.currentStation != null

                val pagerState = rememberPagerState(initialPage = 0) { TOP_LEVEL_TABS.size }
                val scope = rememberCoroutineScope()

                // Stable lambdas so the pager's content slot doesn't recompose on every frame
                val onStationClick = remember(navController) {
                    { _: String ->
                        // launchSingleTop prevents stacked NowPlaying entries when the user
                        // reopens the same destination (e.g., tap station, tap mini player,
                        // tap another station from queue). Without it, the back button could
                        // cycle through several NowPlaying instances.
                        navController.navigate(Screen.NowPlaying.route) { launchSingleTop = true }
                    }
                }
                val onPlaylistClick = remember(navController) {
                    { id: Long -> navController.navigate("playlist/$id") { launchSingleTop = true } }
                }

                // Selected tab index — only changes when the user lands on a new tab,
                // not on every swipe frame. This keeps DotMatrixIcon / Text from
                // recomposing during the swipe gesture.
                val selectedTabIndex by remember(pagerState, currentRoute) {
                    androidx.compose.runtime.derivedStateOf {
                        val idx = TOP_LEVEL_TABS.indexOfFirst { it.route == currentRoute }
                        if (idx >= 0) idx else pagerState.currentPage
                    }
                }

                // Real clearance for list content behind the floating dock:
                // nav-bar inset + dock pill (76dp) + MiniPlayer (~70dp when
                // visible). Overlay routes hide the dock, so only a small
                // bottom breathing room is needed.
                val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                val bottomClearance = if (showOverlay) {
                    navBarInset + 24.dp
                } else {
                    navBarInset + 76.dp + (if (showMiniPlayer) 70.dp else 0.dp)
                }

                Scaffold(
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = com.radiothing.ui.theme.PureBlack
                ) { innerPadding ->
                    CompositionLocalProvider(LocalBottomClearance provides bottomClearance) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Floating dock: TabPager renders full-screen behind the dock so content
                        // shows through the translucent pills. Lists provide their own
                        // bottom contentPadding (≈ dock height) so the last item is not
                        // obscured but still scrolls behind the glass.
                        TabPager(
                            pagerState = pagerState,
                            playerManager = viewModel.playerManager,
                            onStationClick = onStationClick,
                            onPlaylistClick = onPlaylistClick,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )

                        // NavHost must ALWAYS be composed so the NavController's graph
                        // is set from the first frame — navigate() before any NavHost
                        // composition (empty graph) throws and crashes the app.
                        // The transparent overlay_root start destination renders
                        // nothing, so the tab pager shows through and receives input.
                        RadioNavHost(
                            navController = navController,
                            playerManager = viewModel.playerManager,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )

                        // Bottom dock — floating pills that overlay the content. Both are
                        // translucent so the list scrolls underneath.
                        if (!showOverlay) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                            ) {
                                if (showMiniPlayer) {
                                    MiniPlayer(
                                        playerState = playerState,
                                        onPlayPauseClick = { viewModel.playPause() },
                                        onNext = { viewModel.next() },
                                        onPrevious = { viewModel.previous() },
                                        onExpandClick = {
                                            navController.navigate(Screen.NowPlaying.route) { launchSingleTop = true }
                                        },
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .padding(bottom = 8.dp)
                                    )
                                }
                                BottomNavBar(
                                    selectedIndex = selectedTabIndex,
                                    pagerState = pagerState,
                                    onNavigate = { screen ->
                                        val idx = TOP_LEVEL_TABS.indexOf(screen)
                                        if (idx >= 0) {
                                            scope.launch { pagerState.animateScrollToPage(idx) }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}
