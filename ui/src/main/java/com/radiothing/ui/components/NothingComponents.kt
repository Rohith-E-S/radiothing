package com.radiothing.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.radiothing.ui.theme.DotMatrix
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Hairline
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.TextWhite35
import com.radiothing.ui.theme.TextWhite70
import com.radiothing.ui.theme.RadioThingTheme

@Composable
fun NothingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    onSearch: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = DotMatrix,
                color = TextWhite35,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        },
        keyboardOptions = KeyboardOptions(imeAction = if (onSearch != null) ImeAction.Search else ImeAction.Default),
        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Panel,
            unfocusedContainerColor = Panel,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = BrightRed,
            focusedIndicatorColor = BrightRed,
            unfocusedIndicatorColor = GridLine,
            disabledIndicatorColor = GridLine
        ),
        trailingIcon = {
            if (onSearch != null) {
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(BrightRed)
                        .clickable { onSearch.invoke() }
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = "GO",
                        fontFamily = DotMatrix,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        },
        shape = RoundedCornerShape(100.dp),
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .border(1.dp, Hairline, RoundedCornerShape(100.dp))
            .heightIn(min = 48.dp, max = 52.dp)
    )
}

@Composable
fun NothingChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) BrightRed else Panel, RoundedCornerShape(10.dp))
            .border(1.dp, if (isSelected) BrightRed else GridLine, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else TextWhite70,
            fontFamily = DotMatrix,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050507L, name = "Text field states")
@Composable
private fun NothingTextFieldPreview() {
    RadioThingTheme {
        Column(Modifier.padding(16.dp)) {
            NothingTextField(
                value = "",
                onValueChange = {},
                placeholder = "SEARCH",
                onSearch = {},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.heightIn(min = 12.dp))
            NothingTextField(
                value = "groove salad",
                onValueChange = {},
                placeholder = "SEARCH",
                onSearch = {},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.heightIn(min = 12.dp))
            NothingTextField(
                value = "",
                onValueChange = {},
                placeholder = "NO ACTION BUTTON",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050507L, name = "Chip states")
@Composable
private fun NothingChipPreview() {
    RadioThingTheme {
        Row(Modifier.padding(16.dp)) {
            NothingChip(text = "AMBIENT", isSelected = false, onClick = {})
            Spacer(Modifier.width(8.dp))
            NothingChip(text = "JAZZ", isSelected = true, onClick = {})
            Spacer(Modifier.width(8.dp))
            NothingChip(text = "ROCK", isSelected = false, onClick = {})
        }
    }
}
