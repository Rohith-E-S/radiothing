package com.radiothing.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import com.radiothing.ui.theme.Ndot57
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.radiothing.domain.model.AppSettings
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.PureBlack
import com.radiothing.ui.theme.TextWhite35
import com.radiothing.ui.theme.TextWhite70
import com.radiothing.ui.theme.RadioThingTheme
import com.radiothing.ui.common.LocalBottomClearance

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState(initial = null)

    // Read once per screen entry — accurate even without cross-module plumbing
    val context = LocalContext.current
    val appVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        } catch (_: Exception) { "?" }
    }

    SettingsContent(
        appSettings = settings,
        appVersion = appVersion,
        onSetBufferSize = viewModel::setBufferSize,
        onSetUseAsciiNotification = viewModel::setUseAsciiNotification
    )
}

/** Stateless body of the settings screen — hoisted out so it can be previewed without a ViewModel. */
@Composable
private fun SettingsContent(
    appSettings: AppSettings?,
    appVersion: String,
    onSetBufferSize: (Int) -> Unit,
    onSetUseAsciiNotification: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = "SETTINGS",
            color = Color.White,
            fontFamily = Ndot57,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            letterSpacing = 2.5.sp
        )
        Spacer(Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(5.dp).clip(RoundedCornerShape(100.dp)).background(BrightRed))
            Spacer(Modifier.width(6.dp))
            Text("PREFERENCES  •  DEVICE", color = TextWhite35, fontFamily = Ndot57, fontSize = 9.sp, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = GridLine, thickness = 1.dp)
        Spacer(Modifier.height(16.dp))

        appSettings?.let { appSettings ->
            SettingSectionHeader(title = "PLAYBACK")
            SettingsCard {
                SettingSlider(
                    title = "BUFFER SIZE",
                    value = appSettings.bufferSize.toFloat(),
                    range = 1000f..10000f,
                    suffix = "ms",
                    steps = 9,
                    onValueChange = { onSetBufferSize(it.toInt()) }
                )
                HorizontalDivider(color = GridLine)
                SettingSwitch(
                    title = "ASCII NOTIFICATION",
                    subtitle = "Use block art in notification",
                    checked = appSettings.useAsciiNotification,
                    onCheckedChange = { onSetUseAsciiNotification(it) }
                )
            }

            SettingSectionHeader(title = "SLEEP TIMER")
            SettingsCard {
                Text(
                    text = "Use the timer control in Now Playing (5 / 15 / 30 / 60 min). Playback fades and stops.",
                    color = TextWhite70,
                    fontFamily = Ndot57,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            SettingSectionHeader(title = "ABOUT")
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("VERSION", color = TextWhite35, fontFamily = Ndot57, fontSize = 11.sp, letterSpacing = 1.sp)
                        Text(appVersion, color = Color.White, fontFamily = Ndot57, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = GridLine)
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("API", color = TextWhite35, fontFamily = Ndot57, fontSize = 11.sp, letterSpacing = 1.sp)
                        Text("RADIO-BROWSER.INFO", color = Color.White, fontFamily = Ndot57, fontSize = 11.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = GridLine)
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("BUILD", color = TextWhite35, fontFamily = Ndot57, fontSize = 11.sp, letterSpacing = 1.sp)
                        Text("BLACK LAB • 01", color = BrightRed, fontFamily = Ndot57, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "RADIO.THING — OPEN  •  FREE  •  NO ADS",
                color = TextWhite35,
                fontFamily = Ndot57,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 1.5.sp,
                modifier = Modifier.fillMaxWidth()
            )
            // Clear the floating dock so last setting isn't obscured
            Spacer(Modifier.height(LocalBottomClearance.current))
        }
    }
}

@Composable
fun SettingSectionHeader(title: String) {
    Text(
        text = title,
        color = BrightRed,
        fontFamily = Ndot57,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp, top = 16.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Panel)
            .border(1.dp, GridLine, RoundedCornerShape(16.dp)),
        content = content
    )
}

@Composable
fun SettingSwitch(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontFamily = Ndot57, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            if (subtitle != null) {
                Text(text = subtitle, color = TextWhite35, fontFamily = Ndot57, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BrightRed,
                checkedBorderColor = BrightRed,
                uncheckedThumbColor = Color(0xFF9A9A9E),
                uncheckedTrackColor = GridLine,
                uncheckedBorderColor = GridLine
            )
        )
    }
}

@Composable
fun SettingSlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    suffix: String = "",
    steps: Int = 0,
    onValueChange: (Float) -> Unit
) {
    // Hold the thumb locally during a drag; persist once on release. Writing
    // through to the repository on every tick meant dozens of DataStore writes
    // per gesture and a stuttery thumb (value round-tripping through an async
    // flow), and a continuous range produced arbitrary values like 3742ms.
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val display = dragValue ?: value

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, color = Color.White, fontFamily = Ndot57, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = "${display.roundToInt()}$suffix", color = BrightRed, fontFamily = Ndot57, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = display.coerceIn(range.start, range.endInclusive),
            onValueChange = { dragValue = it },
            onValueChangeFinished = {
                dragValue?.let(onValueChange)
                dragValue = null
            },
            valueRange = range,
            steps = steps,
            modifier = Modifier.padding(top = 6.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = BrightRed,
                inactiveTrackColor = GridLine,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000L, device = "spec:width=412dp,height=915dp", name = "Settings screen")
@Composable
private fun SettingsScreenPreview() {
    RadioThingTheme {
        SettingsContent(
            appSettings = AppSettings(useAsciiNotification = true, bufferSize = 5000),
            appVersion = "1.2.0",
            onSetBufferSize = {},
            onSetUseAsciiNotification = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050507L, name = "Setting row states")
@Composable
private fun SettingRowStatesPreview() {
    RadioThingTheme {
        Column(Modifier.padding(16.dp)) {
            SettingSectionHeader(title = "PLAYBACK")
            SettingsCard {
                SettingSwitch(
                    title = "ASCII NOTIFICATION",
                    subtitle = "Use block art in notification",
                    checked = true,
                    onCheckedChange = {}
                )
                HorizontalDivider(color = GridLine)
                SettingSwitch(
                    title = "ASCII NOTIFICATION",
                    checked = false,
                    onCheckedChange = {}
                )
                HorizontalDivider(color = GridLine)
                SettingSlider(
                    title = "BUFFER SIZE",
                    value = 5000f,
                    range = 1000f..10000f,
                    suffix = "ms",
                    steps = 9,
                    onValueChange = {}
                )
            }
        }
    }
}
