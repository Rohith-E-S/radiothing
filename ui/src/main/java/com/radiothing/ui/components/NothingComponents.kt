package com.radiothing.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.radiothing.ui.theme.Ndot57
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Hairline
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.TextWhite35
import com.radiothing.ui.theme.TextWhite70

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
        placeholder = { Text(text = placeholder, fontFamily = Ndot57, color = TextWhite35, fontSize = 13.sp, letterSpacing = 0.8.sp) },
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
                        fontFamily = Ndot57,
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
            fontFamily = Ndot57,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
    }
}
