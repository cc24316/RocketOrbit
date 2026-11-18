package com.rocketorbit.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Orbit {
    val Roxo = Color(0xFF6B2FB5)
    val RoxoEscuro = Color(0xFF1A0A3E)   // fundo
    val Lilas = Color(0xFFC7ADF2)
    val Cream = Color(0xFFF6F2FF)        // texto principal
    val Lavender = Color(0xFFBBA6E6)     // texto secundário
    val Bubble = Color(0xFFEDE5FF)
    val Laranja = Color(0xFFFF9A4A)
    val Dourado = Color(0xFFFFD54F)
    val Coral = Color(0xFFFF7A8A)
    val Mint = Color(0xFF7CE0A3)
    val Glass = Color(0x12FFFFFF)
    val GlassLine = Color(0x26FFFFFF)

    // cartões sólidos (sem transparência)
    val Card = Color(0xFF3A2175)
    val CardEscuro = Color(0xFF2B1B54)
    val CardSelecionado = Color(0xFF5A4BA8)
}

@Composable
fun RocketOrbitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Orbit.Roxo,
            onPrimary = Orbit.Cream,
            secondary = Orbit.Lilas,
            background = Orbit.RoxoEscuro,
            onBackground = Orbit.Cream,
            surface = Orbit.RoxoEscuro,
            onSurface = Orbit.Cream
        )
    ) {
        content()
    }
}
