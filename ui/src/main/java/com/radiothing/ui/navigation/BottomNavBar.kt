package com.radiothing.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import com.radiothing.ui.theme.Ndot57
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.ui.components.DotMatrixIcon
import com.radiothing.ui.components.IconType
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Ink
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.TextWhite35
import com.radiothing.ui.theme.TextWhite70

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    // LAB enclosure: inset pill within ink ground, 1dp hairline, generous air.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ink)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(100.dp))
                .background(Panel)
                .drawBehind {
                    drawRoundRect(
                        color = GridLine,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(100.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                    )
                }
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Triple(Screen.Browse, IconType.BROWSE, "BROWSE"),
                Triple(Screen.Favorites, IconType.FAVORITES, "FAVS"),
                Triple(Screen.Playlists, IconType.FAVORITES, "LISTS"),
                Triple(Screen.History, IconType.HISTORY, "LOG"),
                Triple(Screen.Settings, IconType.SETTINGS, "SET"),
            )

            items.forEach { (screen: Screen, iconType: IconType, label: String) ->
                val isSelected = currentRoute == screen.route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isSelected) Color(0x1AFF3344) else Color.Transparent)
                        .drawBehind {
                            if (isSelected) {
                                drawRoundRect(
                                    color = BrightRed.copy(alpha = 0.45f),
                                    topLeft = Offset.Zero,
                                    size = Size(size.width, size.height),
                                    cornerRadius = CornerRadius(100.dp.toPx()),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                                )
                            }
                        }
                        .clickable(role = Role.Tab) { onNavigate(screen) }
                        .semantics {
                            selected = isSelected
                            contentDescription = label
                            role = Role.Tab
                        }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DotMatrixIcon(
                        type = iconType,
                        color = if (isSelected) BrightRed else TextWhite35,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else TextWhite35,
                        fontFamily = Ndot57,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        lineHeight = 9.sp
                    )
                }
            }
        }
    }
}
