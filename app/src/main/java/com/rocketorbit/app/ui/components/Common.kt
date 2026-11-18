package com.rocketorbit.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rocketorbit.app.data.Planet
import com.rocketorbit.app.ui.theme.Orbit

/** Título grande com degradê, igual ao do OnScreen. */
@Composable
fun GradientTitle(text: String, modifier: Modifier = Modifier, size: Int = 46) {
    Text(
        text,
        modifier = modifier,
        style = TextStyle(
            brush = Brush.linearGradient(listOf(Color.White, Orbit.Lilas)),
            fontSize = size.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1).sp
        )
    )
}

/** Balão de fala do Rocky. */
@Composable
fun SpeechBubble(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .drawBehind {
                val midY = size.height * 0.5f
                val tail = Path().apply {
                    moveTo(2f, midY - 12f)
                    lineTo(-14f, midY)
                    lineTo(2f, midY + 12f)
                    close()
                }
                drawPath(tail, Orbit.Bubble)
            }
            .background(Orbit.Bubble, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(text, color = Orbit.RoxoEscuro, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium)
    }
}

/** Imagem do planeta; [size] é o tamanho da esfera, o anel de Saturno/Urano usa [Planet.scale]. */
@Composable
fun PlanetImage(planet: Planet, size: Dp, modifier: Modifier = Modifier) {
    Image(
        painterResource(planet.image),
        contentDescription = planet.name,
        modifier = modifier.requiredSize(size * planet.scale)
    )
}

@Composable
fun StatChip(icon: ImageVector, text: String, tint: Color) {
    Row(
        Modifier
            .background(Orbit.Glass, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Text(text, color = Orbit.Cream, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
fun BigButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Orbit.Roxo,
            contentColor = Color.White,
            disabledContainerColor = Orbit.Roxo.copy(alpha = 0.35f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        )
    ) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

/** Botão de escolha (duração, meta etc). */
@Composable
fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp
) {
    Box(
        modifier
            .height(height)
            .background(if (selected) Orbit.Roxo else Orbit.Glass, RoundedCornerShape(12.dp))
            .border(
                BorderStroke(1.dp, if (selected) Orbit.Lilas.copy(alpha = 0.7f) else Orbit.GlassLine),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

/**
 * Linha no estilo do OnScreen: barrinha colorida à esquerda, texto e algo à direita.
 */
@Composable
fun InfoRow(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {}
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Orbit.Glass)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(4.dp).fillMaxHeight().background(Orbit.Roxo))
        Column(Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 14.dp)) {
            Text(title, color = Orbit.Lavender, fontSize = 13.sp)
            Text(value, color = Orbit.Cream, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            if (progress != null) {
                Box(
                    Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Orbit.Glass)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(progress.coerceIn(0.02f, 1f))
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Orbit.Lilas)
                    )
                }
            }
        }
        Box(Modifier.padding(end = 14.dp)) { trailing() }
    }
}

@Composable
fun CatSays(mood: Mood, text: String, modifier: Modifier = Modifier, catWidth: Dp = 104.dp) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Rocky(mood, width = catWidth)
        Box(Modifier.padding(start = 16.dp).weight(1f)) { SpeechBubble(text) }
    }
}
