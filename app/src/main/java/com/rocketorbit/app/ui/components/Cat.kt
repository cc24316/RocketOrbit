package com.rocketorbit.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.sin

enum class Mood { Happy, Focus, Worried, Party, Sleep }

/** De noite o Rocky dorme: das 21h às 5h59. */
private const val NIGHT_START_HOUR = 21
private const val NIGHT_END_HOUR = 6

fun isNightHour(hour: Int): Boolean = hour >= NIGHT_START_HOUR || hour < NIGHT_END_HOUR

/** true quando é noite no aparelho. Confere de novo a cada 20 s, então dorme e acorda sozinho com a tela aberta. */
@Composable
fun rememberIsNight(): Boolean {
    var hour by remember { mutableIntStateOf(LocalTime.now().hour) }
    LaunchedEffect(Unit) {
        while (true) {
            hour = LocalTime.now().hour
            delay(20_000)
        }
    }
    return isNightHour(hour)
}

@Composable
fun Rocky(mood: Mood, modifier: Modifier = Modifier, width: Dp = 140.dp) {
    val transition = rememberInfiniteTransition(label = "miau")
    val floatT by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Restart),
        label = "float"
    )
    val blink by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 4200
                1f at 0
                1f at 3900
                0.05f at 4020
                1f at 4140
            }
        ),
        label = "blink"
    )
    val spark by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "spark"
    )
    val zzz by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Restart),
        label = "zzz"
    )
    // dormindo, respira mais devagar e mais fundo
    val bob = sin(floatT * 2f * PI.toFloat()) * (if (mood == Mood.Sleep) 0.6f else 1f)

    Canvas(modifier.size(width, width * 1.15f)) {
        val s = this.size.width / 200f
        scale(s, pivot = Offset.Zero) {
            translate(top = bob * -5f) {
                rotate(bob * 2f, pivot = Offset(100f, 150f)) {
                    drawCat(mood, blink, spark, zzz)
                }
            }
        }
    }
}

private val CatOrange = Color(0xFFFF8A3D)
private val CatOrangeDeep = Color(0xFFD9620F)
private val CatCyan = Color(0xFF5EE0E8)
private val CatCream = Color(0xFFFFF1DC)
private val Suit = Color(0xFFF1EEFF)
private val SuitShade = Color(0xFFCBC3F2)
private val Glove = Color(0xFFE4DFFA)
private val Ink = Color(0xFF1B1033)
private val Mouth = Color(0xFF7A3410)

private fun DrawScope.drawCat(mood: Mood, blink: Float, spark: Float, zzz: Float) {
    val orange = CatOrange
    val party = mood == Mood.Party

    // rabo
    val tail = Path().apply {
        moveTo(138f, 214f)
        cubicTo(170f, 214f, 192f, 192f, 180f, 160f)
    }
    drawPath(tail, orange, style = Stroke(width = 15f, cap = StrokeCap.Round))

    // braços
    val lHand = if (party) Offset(32f, 150f) else Offset(40f, 214f)
    val rHand = if (party) Offset(168f, 150f) else Offset(160f, 214f)
    drawLine(SuitShade, Offset(62f, 184f), lHand, 19f, StrokeCap.Round)
    drawLine(SuitShade, Offset(138f, 184f), rHand, 19f, StrokeCap.Round)
    drawCircle(Glove, 11f, lHand)
    drawCircle(Glove, 11f, rHand)

    // corpo
    drawRoundRect(
        Brush.verticalGradient(listOf(Suit, SuitShade), startY = 166f, endY = 228f),
        topLeft = Offset(54f, 166f), size = Size(92f, 62f), cornerRadius = CornerRadius(30f, 30f)
    )
    drawRoundRect(Color(0xFF1A1245), Offset(86f, 186f), Size(28f, 20f), CornerRadius(6f, 6f))
    drawCircle(CatCyan, 3.5f, Offset(95f, 196f))
    drawCircle(orange, 3.5f, Offset(105f, 196f))

    // cabeça: 14 mais embaixo, encostada no colarinho e com as orelhas dentro do capacete
    translate(top = 14f) { drawHead(mood, blink) }

    // vidro do capacete
    drawCircle(
        Brush.radialGradient(
            listOf(Color.Transparent, Color.White.copy(alpha = 0.13f)),
            center = Offset(100f, 98f), radius = 78f
        ),
        radius = 78f, center = Offset(100f, 98f)
    )
    drawCircle(Color(0xFFBFEFFF).copy(alpha = 0.85f), 78f, Offset(100f, 98f), style = Stroke(width = 5f))
    drawArc(
        Color.White.copy(alpha = 0.55f), 200f, 40f, false,
        Offset(34f, 32f), Size(132f, 132f), style = Stroke(width = 6f, cap = StrokeCap.Round)
    )
    drawCircle(Color.White.copy(alpha = 0.6f), 3.5f, Offset(152f, 56f))

    // colarinho
    drawRoundRect(
        Color(0xFFDAD5F5), Offset(62f, 156f), Size(76f, 20f), CornerRadius(9f, 9f)
    )
    drawRoundRect(
        Color(0xFFB8B0E8), Offset(62f, 156f), Size(76f, 20f), CornerRadius(9f, 9f),
        style = Stroke(width = 2f)
    )
    drawCircle(CatCyan, 3f, Offset(76f, 166f))
    drawCircle(CatCyan, 3f, Offset(124f, 166f))

    // faíscas de festa
    if (party) {
        val spots = listOf(Offset(22f, 40f), Offset(180f, 52f), Offset(190f, 120f), Offset(14f, 118f))
        spots.forEachIndexed { i, p ->
            val phase = (spark + i * 0.25f) % 1f
            val r = 3f + 7f * sin(PI.toFloat() * phase)
            val col = if (i % 2 == 0) CatCream else CatCyan
            drawLine(col, Offset(p.x - r, p.y), Offset(p.x + r, p.y), 2.5f, StrokeCap.Round)
            drawLine(col, Offset(p.x, p.y - r), Offset(p.x, p.y + r), 2.5f, StrokeCap.Round)
        }
    }

    // zzz: três "z" que sobem do lado do capacete, crescem e somem
    if (mood == Mood.Sleep) {
        for (i in 0 until 3) {
            val p = (zzz + i / 3f) % 1f
            val h = 4f + 5f * p
            val cx = 166f + 20f * p
            val cy = 46f - 32f * p
            val z = Path().apply {
                moveTo(cx - h, cy - h); lineTo(cx + h, cy - h); lineTo(cx - h, cy + h); lineTo(cx + h, cy + h)
            }
            drawPath(
                z, Color(0xFFDCE4FF).copy(alpha = sin(PI.toFloat() * p).coerceIn(0f, 1f)),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

private fun DrawScope.drawHead(mood: Mood, blink: Float) {
    val party = mood == Mood.Party
    val orange = CatOrange

    // orelhas
    val earL = Path().apply { moveTo(52f, 70f); lineTo(60f, 26f); lineTo(92f, 48f); close() }
    val earR = Path().apply { moveTo(148f, 70f); lineTo(140f, 26f); lineTo(108f, 48f); close() }
    listOf(earL, earR).forEach {
        drawPath(it, orange)
        drawPath(it, orange, style = Stroke(width = 8f, join = StrokeJoin.Round))
    }
    val innerL = Path().apply { moveTo(60f, 60f); lineTo(64f, 38f); lineTo(82f, 50f); close() }
    val innerR = Path().apply { moveTo(140f, 60f); lineTo(136f, 38f); lineTo(118f, 50f); close() }
    drawPath(innerL, Color(0xFFFFB48A))
    drawPath(innerR, Color(0xFFFFB48A))

    // cabeça
    drawOval(
        Brush.radialGradient(
            listOf(Color(0xFFFFA25A), Color(0xFFF27A24)),
            center = Offset(100f, 80f), radius = 70f
        ),
        topLeft = Offset(46f, 48f), size = Size(108f, 100f)
    )
    // listras da testa
    listOf(
        Offset(100f, 54f) to Offset(100f, 68f),
        Offset(88f, 56f) to Offset(90f, 68f),
        Offset(112f, 56f) to Offset(110f, 68f)
    ).forEach { (a, b) -> drawLine(CatOrangeDeep, a, b, 5f, StrokeCap.Round) }

    // focinho
    drawOval(CatCream, topLeft = Offset(70f, 100f), size = Size(60f, 40f))
    val nose = Path().apply { moveTo(94f, 104f); lineTo(106f, 104f); lineTo(100f, 112f); close() }
    drawPath(nose, Color(0xFFFF7A8A))
    drawPath(nose, Color(0xFFFF7A8A), style = Stroke(width = 3f, join = StrokeJoin.Round))

    // boca
    val mouthStroke = Stroke(width = 2.5f, cap = StrokeCap.Round)
    when (mood) {
        Mood.Party -> {
            val open = Path().apply {
                moveTo(91f, 117f); quadraticTo(100f, 136f, 109f, 117f); close()
            }
            drawPath(open, Color(0xFF7A2A10))
            drawOval(Color(0xFFFF8FA0), Offset(95f, 124f), Size(10f, 7f))
        }
        Mood.Worried -> {
            val m = Path().apply {
                moveTo(100f, 114f); lineTo(100f, 117f)
                moveTo(90f, 124f); quadraticTo(100f, 115f, 110f, 124f)
            }
            drawPath(m, Mouth, style = mouthStroke)
        }
        else -> {
            val m = Path().apply {
                moveTo(100f, 112f); lineTo(100f, 117f)
                moveTo(100f, 117f); quadraticTo(94f, 124f, 88f, 116f)
                moveTo(100f, 117f); quadraticTo(106f, 124f, 112f, 116f)
            }
            drawPath(m, Mouth, style = mouthStroke)
        }
    }

    // bigodes
    val whisker = Color.White.copy(alpha = 0.85f)
    drawLine(whisker, Offset(74f, 112f), Offset(42f, 106f), 2f, StrokeCap.Round)
    drawLine(whisker, Offset(74f, 118f), Offset(42f, 121f), 2f, StrokeCap.Round)
    drawLine(whisker, Offset(126f, 112f), Offset(158f, 106f), 2f, StrokeCap.Round)
    drawLine(whisker, Offset(126f, 118f), Offset(158f, 121f), 2f, StrokeCap.Round)

    // bochechas
    if (mood == Mood.Happy || mood == Mood.Sleep || party) {
        drawCircle(Color(0xFFFF6F6F).copy(alpha = 0.28f), 8f, Offset(64f, 108f))
        drawCircle(Color(0xFFFF6F6F).copy(alpha = 0.28f), 8f, Offset(136f, 108f))
    }

    // olhos
    val arc = Stroke(width = 4.5f, cap = StrokeCap.Round)
    listOf(80f, 120f).forEach { cx ->
        val cy = 92f
        when (mood) {
            Mood.Happy, Mood.Party ->
                drawArc(Ink, 200f, 140f, false, Offset(cx - 9f, cy - 4f), Size(18f, 16f), style = arc)
            Mood.Sleep ->
                drawArc(Ink, 20f, 140f, false, Offset(cx - 9f, cy - 10f), Size(18f, 16f), style = arc)
            else -> {
                val h = (if (mood == Mood.Worried) 23f else 17f) * blink
                val hh = h.coerceAtLeast(2.5f)
                drawOval(Ink, Offset(cx - 7f, cy - hh / 2f), Size(14f, hh))
                if (blink > 0.5f) drawCircle(Color.White, 3.2f, Offset(cx - 2f, cy - hh / 2f + 5f))
            }
        }
    }
    // sobrancelhas
    if (mood == Mood.Worried) {
        drawLine(CatOrangeDeep, Offset(68f, 82f), Offset(90f, 73f), 3.5f, StrokeCap.Round)
        drawLine(CatOrangeDeep, Offset(110f, 73f), Offset(132f, 82f), 3.5f, StrokeCap.Round)
        val drop = Path().apply {
            moveTo(142f, 60f); quadraticTo(150f, 74f, 142f, 78f); quadraticTo(134f, 74f, 142f, 60f); close()
        }
        drawPath(drop, CatCyan.copy(alpha = 0.9f))
    }
    if (mood == Mood.Focus) {
        drawLine(CatOrangeDeep, Offset(68f, 78f), Offset(91f, 80f), 3.5f, StrokeCap.Round)
        drawLine(CatOrangeDeep, Offset(109f, 80f), Offset(132f, 78f), 3.5f, StrokeCap.Round)
    }
}