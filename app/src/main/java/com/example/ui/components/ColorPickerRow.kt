package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ColorPickerRow(
    label: String,
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val presetColors = listOf(
        "#1E293B", "#0F172A", "#10B981", "#3B82F6", "#8B5CF6",
        "#EC4899", "#F59E0B", "#EF4444", "#FFFFFF", "#000000",
        "#1E1B4B", "#064E3B", "#451A03", "#312E81", "#831843"
    )

    var showCustomDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                val parsedColor = remember(selectedColorHex) {
                    try { Color(android.graphics.Color.parseColor(selectedColorHex)) } catch (e: Exception) { Color.Gray }
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(parsedColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { showCustomDialog = true }) {
                    Text("كود $selectedColorHex", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(presetColors) { hex ->
                val color = remember(hex) {
                    try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
                }
                val isSelected = hex.equals(selectedColorHex, ignoreCase = true)

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(hex) }
                )
            }
        }
    }

    if (showCustomDialog) {
        var inputHex by remember { mutableStateOf(selectedColorHex) }
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("أدخل كود اللون Hex") },
            text = {
                OutlinedTextField(
                    value = inputHex,
                    onValueChange = { inputHex = it },
                    label = { Text("مثال: #F59E0B") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val formatted = if (!inputHex.startsWith("#")) "#$inputHex" else inputHex
                        onColorSelected(formatted)
                        showCustomDialog = false
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
