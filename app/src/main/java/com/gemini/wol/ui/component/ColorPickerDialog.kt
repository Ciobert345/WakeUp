package com.gemini.wol.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import android.graphics.Color as AndroidColor

@Composable
fun ColorPickerDialog(
    initialColor: Int,
    onDismiss: () -> Unit,
    onColorSelected: (Int) -> Unit
) {
    var currentColor by remember { mutableStateOf(Color(initialColor)) }
    
    // HSV components - Full control
    val hsv = remember { FloatArray(3) }
    AndroidColor.colorToHSV(initialColor, hsv)
    
    var hue by remember { mutableStateOf(hsv[0]) }
    var saturation by remember { mutableStateOf(hsv[1]) }
    var value by remember { mutableStateOf(hsv[2]) }
    
    // Update color when any HSV component changes
    fun updateColor() {
        hsv[0] = hue
        hsv[1] = saturation
        hsv[2] = value
        currentColor = Color(AndroidColor.HSVToColor(hsv))
    }

    // Hex String
    var hexString by remember { mutableStateOf(String.format("%06X", (0xFFFFFF and initialColor))) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.width(340.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Accent Color",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Color Preview with border
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    currentColor.copy(alpha = 1f),
                                    currentColor.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .border(3.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner circle for depth effect
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(currentColor)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Hue Slider with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Hue",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hue",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        RainbowSlider(
                            hue = hue,
                            onHueChanged = { 
                                hue = it
                                updateColor()
                                hexString = String.format("%06X", (0xFFFFFF and currentColor.toArgb()))
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Saturation Slider with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Saturation",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Saturation",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SaturationSlider(
                            hue = hue,
                            saturation = saturation,
                            value = value,
                            onSaturationChanged = { 
                                saturation = it
                                updateColor()
                                hexString = String.format("%06X", (0xFFFFFF and currentColor.toArgb()))
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Brightness/Value Slider with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Brightness4,
                        contentDescription = "Brightness",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Brightness",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        BrightnessSlider(
                            hue = hue,
                            saturation = saturation,
                            value = value,
                            onValueChanged = { 
                                value = it
                                updateColor()
                                hexString = String.format("%06X", (0xFFFFFF and currentColor.toArgb()))
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // HEX Input
                OutlinedTextField(
                    value = hexString,
                    onValueChange = { input ->
                        if (input.length <= 6) {
                            hexString = input.uppercase()
                            if (hexString.length == 6) {
                                try {
                                    val parsedColor = android.graphics.Color.parseColor("#$hexString")
                                    currentColor = Color(parsedColor)
                                    AndroidColor.colorToHSV(parsedColor, hsv)
                                    hue = hsv[0]
                                    saturation = hsv[1]
                                    value = hsv[2]
                                } catch (e: Exception) {
                                    // Invalid hex
                                }
                            }
                        }
                    },
                    label = { Text("HEX Code") },
                    prefix = { Text("#") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = currentColor,
                        focusedLabelColor = currentColor
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onColorSelected(currentColor.toArgb()) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentColor
                        )
                    ) {
                        Text("Select")
                    }
                }
            }
        }
    }
}

@Composable
fun RainbowSlider(
    hue: Float,
    onHueChanged: (Float) -> Unit
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
        )
    )
    
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(gradient)
                .border(0.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
        )
        Slider(
            value = hue,
            onValueChange = onHueChanged,
            valueRange = 0f..360f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SaturationSlider(
    hue: Float,
    saturation: Float,
    value: Float,
    onSaturationChanged: (Float) -> Unit
) {
    val hsv = FloatArray(3).apply {
        this[0] = hue
        this[1] = 0f // Will change
        this[2] = value
    }
    
    val gradient = Brush.horizontalGradient(
        colors = List(11) { index ->
            hsv[1] = index / 10f
            Color(AndroidColor.HSVToColor(hsv))
        }
    )
    
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(gradient)
                .border(0.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
        )
        Slider(
            value = saturation,
            onValueChange = onSaturationChanged,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BrightnessSlider(
    hue: Float,
    saturation: Float,
    value: Float,
    onValueChanged: (Float) -> Unit
) {
    val hsv = FloatArray(3).apply {
        this[0] = hue
        this[1] = saturation
        this[2] = 0f // Will change
    }
    
    val gradient = Brush.horizontalGradient(
        colors = List(11) { index ->
            hsv[2] = index / 10f
            Color(AndroidColor.HSVToColor(hsv))
        }
    )
    
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(gradient)
                .border(0.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
        )
        Slider(
            value = value,
            onValueChange = onValueChanged,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
