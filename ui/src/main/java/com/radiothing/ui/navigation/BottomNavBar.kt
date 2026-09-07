package com.radiothing.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import com.radiothing.ui.theme.DotMatrix
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.ui.components.DotMatrixIcon
import com.radiothing.ui.components.IconType
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.TextWhite35
import com.radiothing.ui.theme.RadioThingTheme
import androidx.compose.ui.tooling.preview.Preview

private data class TabItem(val screen: Screen, val iconType: IconType, val label: String)

private val TAB_ITEMS = listOf(
    TabItem(Screen.Browse, IconType.BROWSE, "BROWSE"),
    TabItem(Screen.Favorites, IconType.FAVORITES, "FAVS"),
    TabItem(Screen.Playlists, IconType.PLAYLISTS, "LISTS"),
    TabItem(Screen.History, IconType.HISTORY, "LOG"),
    TabItem(Screen.Settings, IconType.SETTINGS, "SET"),
)

/**
 * Floating bottom navigation bar. Sits above the content with a translucent
 * blurred panel, inset from the screen edges and the system navigation bar.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomNavBar(
    selectedIndex: Int,
    pagerState: PagerState,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemCount = TAB_ITEMS.size
    // Capture the PagerState in a stable reference so the layout block can read
    // currentPageOffsetFraction on every layout pass without re-triggering
    // recomposition of the whole bar.
    val stablePagerState = remember(pagerState) { pagerState }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .padding(bottom = 12.dp)
    ) {
        // Floating pill — opaque to avoid per-frame alpha blend behind list (120Hz overdraw tax)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Panel)
        ) {
            // Animated red pill — reads pagerState inside the layout block so it
            // animates per frame without re-running the rest of the bar's composition.
            Box(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val progress = (stablePagerState.currentPage +
                            stablePagerState.currentPageOffsetFraction)
                            .coerceIn(0f, (itemCount - 1).toFloat())
                        val itemWidth = constraints.maxWidth / itemCount
                        val pad = 4.dp.roundToPx()
                        val pillWidth = itemWidth - pad * 2
                        val pillHeight = constraints.maxHeight - pad * 2
                        val x = (itemWidth * progress).toInt() + pad
                        val placeable = measurable.measure(
                            Constraints.fixed(pillWidth.coerceAtLeast(1), pillHeight.coerceAtLeast(1))
                        )
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.placeRelative(x, pad)
                        }
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0x1AFF3344))
            )

            // Tab buttons — only recompose when selectedIndex changes
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TAB_ITEMS.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(28.dp))
                            .clickable(role = Role.Tab) { onNavigate(item.screen) }
                            .semantics {
                                selected = isSelected
                                contentDescription = item.label
                            }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        DotMatrixIcon(
                            type = item.iconType,
                            color = if (isSelected) BrightRed else TextWhite35,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = item.label,
                            color = if (isSelected) Color.White else TextWhite35,
                            fontFamily = DotMatrix,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 8.5.sp,
                            letterSpacing = 0.8.sp,
                            lineHeight = 8.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000L, name = "Browse selected")
@Composable
private fun BottomNavBarPreview() {
    BottomNavBarPreviewContent(selectedIndex = 0)
}

@Preview(showBackground = true, backgroundColor = 0xFF000000L, name = "Playlists selected")
@Composable
private fun BottomNavBarPlaylistsSelectedPreview() {
    BottomNavBarPreviewContent(selectedIndex = 2)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BottomNavBarPreviewContent(selectedIndex: Int) {
    RadioThingTheme {
        Box(Modifier.fillMaxSize()) {
            BottomNavBar(
                selectedIndex = selectedIndex,
                pagerState = rememberPagerState(pageCount = { TAB_ITEMS.size }),
                onNavigate = {},
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
