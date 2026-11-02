package com.proyecto.epico.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    secondary = Color(0xFF2563EB),
    tertiary = Color(0xFF22D3EE),
    background = Color(0xFF061224),
    surface = Color(0xFF0E1F36),
    onPrimary = Color(0xFF052F3F),
    onSecondary = Color.White,
    onTertiary = Color(0xFF052F3F),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun ProyectoepicoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
