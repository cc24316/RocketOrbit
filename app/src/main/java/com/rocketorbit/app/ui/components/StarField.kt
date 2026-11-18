package com.rocketorbit.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.rocketorbit.app.ui.theme.Orbit
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private class Star(
    val x: Float, val y: Float, val r: Float,
    val phase: Float, val cycles: Int, val tint: Color
)

private class Field(val dots: List<Star>, val sparkles: List<Star>)

private val Tints = listOf(
    Color.White, Color.White, Color.White,
    Color(0xFFDCCBFF),   // lilás
    Color(0xFFFFEBB0)    // amarelinho
)

private fun buildField(): Field {
    val rnd = Random(11)
    val dots = List(110) {
        Star(
            x = rnd.nextFloat(), y = rnd.nextFloat(),
            r = 0.7f + rnd.nextFloat() * 1.5f,
            phase = rnd.nextFloat(), cycles = 1 + rnd.nextInt(3),
            tint = Tints[rnd.nextInt(Tints.size)]
        )
    }
    val sparkles = List(9) {
        Star(
            x = 0.06f + rnd.nextFloat() * 0.88f, y = 0.04f + rnd.nextFloat() * 0.92f,
            r = 5f + rnd.nextFloat() * 4f,
            phase = rnd.nextFloat(), cycles = 1 + rnd.nextInt(2),
            tint = Tints[rnd.nextInt(Tints.size)]
        )
    }
    return Field(dots, sparkles)
}

private fun wave(t: Float, s: Star): Float =
    0.5f + 0.5f * sin(2f * PI.toFloat() * (t * s.cycles + s.phase))

/** Brilhinho de 4 pontas. */
private fun sparklePath(cx: Float, cy: Float, r: Float): Path {
    val k = r * 0.3f
    return Path().apply {
        moveTo(cx, cy - r)
        lineTo(cx + k, cy - k)
        lineTo(cx + r, cy)
        lineTo(cx + k, cy + k)
        lineTo(cx, cy + r)
        lineTo(cx - k, cy + k)
        lineTo(cx - r, cy)
        lineTo(cx - k, cy - k)
        close()
    }
}

/** Fundo roxo com estrelas que piscam, cada uma no seu ritmo. */
@Composable
fun StarField(modifier: Modifier = Modifier.fillMaxSize()) {
    val field = remember { buildField() }
    val transition = rememberInfiniteTransition(label = "stars")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "twinkle"
    )

    Canvas(modifier) {
        drawRect(Orbit.RoxoEscuro)
        // brilhos suaves de profundidade
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFF3A1A80).copy(alpha = 0.55f), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.08f),
                radius = size.width * 0.95f
            ),
            radius = size.width * 0.95f,
            center = Offset(size.width * 0.85f, size.height * 0.08f)
        )
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFF5B2A9E).copy(alpha = 0.30f), Color.Transparent),
                center = Offset(size.width * 0.05f, size.height * 0.95f),
                radius = size.width * 0.9f
            ),
            radius = size.width * 0.9f,
            center = Offset(size.width * 0.05f, size.height * 0.95f)
        )

        field.dots.forEach { s ->
            val a = 0.2f + 0.65f * wave(t, s)
            drawCircle(
                s.tint.copy(alpha = a),
                radius = s.r * density,
                center = Offset(s.x * size.width, s.y * size.height)
            )
        }

        field.sparkles.forEach { s ->
            val w = wave(t, s)
            val cx = s.x * size.width
            val cy = s.y * size.height
            val r = s.r * density * (0.6f + 0.4f * w)
            val a = 0.3f + 0.7f * w
            drawCircle(
                Brush.radialGradient(
                    listOf(s.tint.copy(alpha = 0.25f * w), Color.Transparent),
                    center = Offset(cx, cy), radius = r * 2.6f
                ),
                radius = r * 2.6f, center = Offset(cx, cy)
            )
            val p = sparklePath(cx, cy, r)
            drawPath(p, s.tint.copy(alpha = a))
            drawPath(p, s.tint.copy(alpha = a), style = Stroke(width = r * 0.25f, join = StrokeJoin.Round))
        }
    }
}
