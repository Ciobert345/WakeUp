package com.gemini.wol.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
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
    
    // HSV components
    // We'll primarily allow changing Hue. Saturation/Value usually kept high for accent.
    // Simplifying: 1 Slider for Hue (0..360)
    
    val hsv = remember { FloatArray(3) }
    AndroidColor.colorToHSV(initialColor, hsv)
    var hue by remember { mutableStateOf(hsv[0]) }
    
    // Update color when hue changes
    fun updateColor(newHue: Float) {
        hue = newHue
        hsv[0] = newHue
        hsv[1] = 1f // Full saturation
        hsv[2] = 1f // Full value
        currentColor = Color(AndroidColor.HSVToColor(hsv))
    }

    // Hex String
    var hexString by remember { mutableStateOf(String.format("%06X", (0xFFFFFF and initialColor))) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.width(320.dp)
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
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Color Preview
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(currentColor)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Hue Slider (Rainbow)
                RainbowSlider(
                    hue = hue,
                    onHueChanged = { 
                        updateColor(it)
                        hexString = String.format("%06X", (0xFFFFFF and currentColor.toArgb()))
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
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
                                } catch (e: Exception) {
                                    // Invalid hex
                                }
                            }
                        }
                    },
                    label = { Text("HEX Code") },
                    prefix = { Text("#") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onColorSelected(currentColor.toArgb()) }) {
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
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
    ) {
        val width = maxWidth
        
        // Track
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(gradient)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newHue = (offset.x / size.width) * 360f
                        onHueChanged(newHue.coerceIn(0f, 360f))
                    }
                }
        ) {
            
        }
        
        // Thumb logic would go here, simplified to tap for now or draggable
        // Let's implement draggable
    }
    
    // Simplified Slider using standard slider but with custom track? 
    // Actually, let's use a standard Slider and map value 0..360, but modifying track is hard.
    // Let's stick to the Box above plus a draggable thumb indicator.
    
    // ... For simplicity in this iteration, I'll use a standard Slider and tint it? 
    // No, standard slider is one color.
    
    // Re-implementing a simple drag behavior on the Box above:
    var dragOffset by remember { mutableStateOf(0f) }
    
    // We need to know the width to map hue to offset, complicating things in Compose without `onGloballyPositioned`.
    // Let's use a standard Slider but put the rainbow BEHIND it and make the track transparent?
    
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(gradient)
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
