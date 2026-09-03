package com.radiothing.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.domain.model.StationOrder
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Ink
import com.radiothing.ui.theme.Ndot57
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.TextWhite35
import com.radiothing.ui.theme.TextWhite70

/** Selection state carried between Browse and the FilterSheet. */
data class FilterSelection(
    val bitrates: Set<String> = emptySet(),
    val codecs: Set<String> = emptySet(),
    val country: String? = null,
    val tag: String? = null,
    val language: String? = null,
    val order: StationOrder = StationOrder.VOTES
) {
    val hasServerFilters: Boolean get() = country != null || tag != null || language != null
    val hasAnyFilters: Boolean get() = hasServerFilters || bitrates.isNotEmpty() || codecs.isNotEmpty()
}

@Composable
private fun FilterSectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = Ndot57,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = TextWhite35,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun FilterChipRow(
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { item ->
            val isSelected = selected.contains(item)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) BrightRed else Panel)
                    .border(1.dp, if (isSelected) BrightRed else GridLine, RoundedCornerShape(10.dp))
                    .clickable { onToggle(item) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(item.uppercase(), color = if (isSelected) Color.White else TextWhite70, fontFamily = Ndot57, fontSize = 11.sp, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun FilterChipRowSingle(
    options: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { item ->
            val isSelected = selected == item
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) BrightRed else Panel)
                    .border(1.dp, if (isSelected) BrightRed else GridLine, RoundedCornerShape(10.dp))
                    .clickable { onSelect(if (isSelected) null else item) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(item.uppercase(), color = if (isSelected) Color.White else TextWhite70, fontFamily = Ndot57, fontSize = 11.sp, letterSpacing = 1.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    onApply: (FilterSelection) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    initial: FilterSelection = FilterSelection(),
    countries: List<String> = emptyList(),
    tags: List<String> = emptyList(),
    languages: List<String> = emptyList()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedBitrates by remember(initial.bitrates) { mutableStateOf(initial.bitrates) }
    var selectedCodecs by remember(initial.codecs) { mutableStateOf(initial.codecs) }
    var selectedCountry by remember(initial.country) { mutableStateOf(initial.country) }
    var selectedTag by remember(initial.tag) { mutableStateOf(initial.tag) }
    var selectedLanguage by remember(initial.language) { mutableStateOf(initial.language) }
    var selectedOrder by remember(initial.order) { mutableStateOf(initial.order) }

    fun toggleBitrate(v: String) {
        selectedBitrates = if (v == "Any") emptySet()
        else if (selectedBitrates.contains(v)) selectedBitrates - v
        else selectedBitrates + v
    }
    fun toggleCodec(v: String) {
        selectedCodecs = if (v == "Any") emptySet()
        else if (selectedCodecs.contains(v)) selectedCodecs - v
        else selectedCodecs + v
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0A0A0C),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "FILTERS",
                fontFamily = Ndot57,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(18.dp))

            run {
                FilterSectionLabel("COUNTRY  —  TUNE THE WORLD  •  OR TYPE COUNTRY: IN SEARCH")
                if (countries.isEmpty()) {
                    Text("LOADING COUNTRIES…", color = TextWhite35, fontFamily = Ndot57, fontSize = 11.sp)
                } else {
                    FilterChipRowSingle(
                        options = countries.take(28),
                        selected = selectedCountry,
                        onSelect = { selectedCountry = it }
                    )
                    if (selectedCountry != null && selectedCountry !in countries.take(28)) {
                        Spacer(Modifier.height(4.dp))
                        Text("» ${selectedCountry!!.uppercase()}", color = BrightRed, fontFamily = Ndot57, fontSize = 10.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))
                FilterSectionLabel("TAG  —  GENRE SEARCH  •  OR TYPE TAG: IN SEARCH")
                if (tags.isEmpty()) {
                    Text("LOADING TAGS…", color = TextWhite35, fontFamily = Ndot57, fontSize = 11.sp)
                } else {
                    FilterChipRowSingle(
                        options = tags.take(28),
                        selected = selectedTag,
                        onSelect = { selectedTag = it }
                    )
                }

                Spacer(Modifier.height(16.dp))
                FilterSectionLabel("LANGUAGE  •  OR TYPE LANG: IN SEARCH")
                if (languages.isEmpty()) {
                    Text("LOADING LANGUAGES…", color = TextWhite35, fontFamily = Ndot57, fontSize = 11.sp)
                } else {
                    FilterChipRowSingle(
                        options = languages.take(28),
                        selected = selectedLanguage,
                        onSelect = { selectedLanguage = it }
                    )
                }

                Spacer(Modifier.height(16.dp))
                FilterSectionLabel("ORDER")
                FilterChipRowSingle(
                    options = StationOrder.entries.map { it.label },
                    selected = selectedOrder.label,
                    onSelect = { label -> StationOrder.entries.firstOrNull { it.label == label }?.let { selectedOrder = it } }
                )

                Spacer(Modifier.height(16.dp))
                FilterSectionLabel("BITRATE  —  MULTI-SELECT")
                FilterChipRow(
                    options = listOf("Any", "64", "128", "192", "256", "320"),
                    selected = selectedBitrates,
                    onToggle = { toggleBitrate(it) }
                )

                Spacer(Modifier.height(16.dp))
                FilterSectionLabel("CODEC  —  MULTI-SELECT")
                FilterChipRow(
                    options = listOf("Any", "MP3", "AAC", "OGG", "FLAC"),
                    selected = selectedCodecs,
                    onToggle = { toggleCodec(it) }
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedBitrates = emptySet()
                        selectedCodecs = emptySet()
                        selectedCountry = null
                        selectedTag = null
                        selectedLanguage = null
                        selectedOrder = StationOrder.VOTES
                        onClear()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("CLEAR", fontFamily = Ndot57, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        onApply(
                            FilterSelection(
                                bitrates = selectedBitrates,
                                codecs = selectedCodecs,
                                country = selectedCountry,
                                tag = selectedTag,
                                language = selectedLanguage,
                                order = selectedOrder
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrightRed,
                        contentColor = Color.White
                    )
                ) {
                    Text("APPLY", fontFamily = Ndot57, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
