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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

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
        placeholder = { Text(text = placeholder, fontFamily = FontFamily.Monospace, color = Color.Gray) },
        keyboardOptions = KeyboardOptions(imeAction = if (onSearch != null) ImeAction.Search else ImeAction.Default),
        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1A1A1A),
            unfocusedContainerColor = Color(0xFF1A1A1A),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFFFF2D2D),
            focusedIndicatorColor = Color(0xFFFF2D2D),
            unfocusedIndicatorColor = Color(0xFF333333)
        ),
        trailingIcon = {
            if (onSearch != null) {
                Text(
                    text = "GO",
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFFF2D2D),
                    modifier = Modifier
                        .clickable { onSearch.invoke() }
                        .padding(8.dp)
                )
            }
        },
        modifier = modifier.border(1.dp, Color(0xFF333333))
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
            .clickable(onClick = onClick)
            .background(if (isSelected) Color(0xFFFF2D2D) else Color.Transparent, RoundedCornerShape(2.dp))
            .border(1.dp, if (isSelected) Color(0xFFFF2D2D) else Color(0xFF333333), RoundedCornerShape(2.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontFamily = FontFamily.Monospace
        )
    }
}
