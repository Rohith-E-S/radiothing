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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.radiothing.ui.theme.RadioThingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result if needed
    }
    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK)
        )
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            RadioThingTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val playerState by viewModel.playerState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                                // Will show rationale via snackbar on deny; request directly for now
                            }
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    // Visualizer needs RECORD_AUDIO for real stream waveform — request once, fallback is synthetic
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                LaunchedEffect(playerState.error) {
                    playerState.error?.let {
                        snackbarHostState.showSnackbar(it)
                    }
                }

                val isNowPlayingScreen = currentRoute == Screen.NowPlaying.route
                val showBottomNav = !isNowPlayingScreen
                val showMiniPlayer = !isNowPlayingScreen && playerState.currentStation != null

                Scaffold(
                    // edge-to-edge: don't add systemBars to innerPadding — each screen handles its own status inset
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        if (showBottomNav) {
                            BottomNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { screen ->
                                    navController.navigate(screen.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                    }
                                }
                            )
                        }
                    },
                    containerColor = com.radiothing.ui.theme.PureBlack
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        RadioNavHost(
                            navController = navController,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(bottom = if (showMiniPlayer) 72.dp else 0.dp)
                        )

                        if (showMiniPlayer && playerState.currentStation != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = innerPadding.calculateBottomPadding())
                            ) {
                                MiniPlayer(
                                    playerState = playerState,
                                    onPlayPauseClick = { viewModel.playPause() },
                                    onNext = { viewModel.next() },
                                    onPrevious = { viewModel.previous() },
                                    onExpandClick = { navController.navigate(Screen.NowPlaying.route) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
