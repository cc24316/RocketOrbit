package com.rocketorbit.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.rocketorbit.app.AppState
import com.rocketorbit.app.R
import com.rocketorbit.app.Reward
import com.rocketorbit.app.data.ActiveSession
import com.rocketorbit.app.data.MAX_BREAKS
import com.rocketorbit.app.data.MAX_BREAK_LEN
import com.rocketorbit.app.data.MAX_INTERVAL
import com.rocketorbit.app.data.MIN_MISSION_FOR_BREAKS
import com.rocketorbit.app.data.Planet
import com.rocketorbit.app.data.Planets
import com.rocketorbit.app.data.formatClock
import com.rocketorbit.app.data.formatLong
import com.rocketorbit.app.data.formatMinutes
import com.rocketorbit.app.data.pauseLimits
import com.rocketorbit.app.data.phaseAt
import com.rocketorbit.app.data.planetByName
import com.rocketorbit.app.data.planetForMinutes
import com.rocketorbit.app.ui.components.BigButton
import com.rocketorbit.app.ui.components.Rocky
import com.rocketorbit.app.ui.components.CatSays
import com.rocketorbit.app.ui.components.Mood
import com.rocketorbit.app.ui.components.PlanetImage
import com.rocketorbit.app.ui.components.WheelPicker
import com.rocketorbit.app.ui.theme.Orbit
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private val TituloSecao = Color(0xFFC8C5FF)
private val TextoFaixa = Color(0xFFAAA4E8)
private val Sos = Color(0xFFD30505)

private const val CUSTOM = "custom"
private const val MAX_TOTAL_MINUTES = 600

// valores das rodas (constantes, para a roda não ser refeita a cada recomposição)
private val HoursValues = (0..10).toList()
private val MinutesValues = (0..59).toList()
private val BreaksValues = (0..MAX_BREAKS).toList()
private val IntervalValues = (5..MAX_INTERVAL step 5).toList()
private val BreakLenValues = (1..MAX_BREAK_LEN).toList()

@Composable
fun FocusScreen(state: AppState, onExit: () -> Unit) {
    var reward by remember { mutableStateOf<Reward?>(null) }
    val finished = reward
    val ses = state.session
    when {
        finished != null -> DoneView(finished, onExit)
        ses != null -> RunningView(state, ses) { reward = it }
        else -> SetupView(state, onExit)
    }
}

// ---------------------------------------------------------------- Nova missão

@Composable
private fun SetupView(state: AppState, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var chosen by remember { mutableStateOf("") }          // nome do planeta ou CUSTOM
    var planetMinutes by remember { mutableIntStateOf(0) }
    var customH by remember { mutableIntStateOf(0) }
    var customM by remember { mutableIntStateOf(25) }
    // o que o usuário pediu para as pausas; o que aparece (lim) é sempre já corrigido para caber na missão
    var wantBreaks by remember { mutableIntStateOf(0) }
    var wantInterval by remember { mutableIntStateOf(25) }
    var wantBreakLen by remember { mutableIntStateOf(5) }
    BackHandler(onBack = onBack)

    // 10 h é o máximo: nesse caso os minutos ficam em 0
    val customMShown = if (customH >= MAX_TOTAL_MINUTES / 60) 0 else customM
    val effMinutes = when (chosen) {
        "" -> 0
        CUSTOM -> customH * 60 + customMShown
        else -> planetMinutes
    }
    val valid = chosen.isNotEmpty() && effMinutes in 1..MAX_TOTAL_MINUTES
    val destino: Planet? = when {
        chosen.isEmpty() -> null
        chosen == CUSTOM -> planetForMinutes(effMinutes)
        else -> planetByName(chosen)
    }
    val lim = pauseLimits(effMinutes, wantBreaks, wantInterval, wantBreakLen)

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("‹ Voltar", color = Orbit.Lavender, fontSize = 16.sp) }

        Text("Nova missão", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = title, onValueChange = { title = it },
            label = { Text("Nome da missão (opcional)") },
            singleLine = true, shape = RoundedCornerShape(14.dp),
            colors = fieldColors(), modifier = Modifier.fillMaxWidth()
        )

        Text("Escolha o seu destino:", color = TituloSecao, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Planets.chunked(3).forEach { linha ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                linha.forEach { p ->
                    PlanetCard(p, selected = chosen == p.name, modifier = Modifier.weight(1f)) {
                        chosen = p.name
                        planetMinutes = p.defaultMinutes
                    }
                }
            }
        }

        // Tempo personalizado: roda de horas e de minutos, como no alarme
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (chosen == CUSTOM) Orbit.CardSelecionado else Orbit.CardEscuro)
                .clickable { chosen = CUSTOM }
                .padding(12.dp)
        ) {
            Text("Personalizado", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Escolha as suas horas e minutos", color = TextoFaixa, fontSize = 12.sp)
            if (chosen == CUSTOM) {
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WheelPicker(
                        label = "Horas", values = HoursValues, selected = customH,
                        maxEnabled = MAX_TOTAL_MINUTES / 60, format = { "$it h" },
                        onChange = { customH = it }, modifier = Modifier.weight(1f)
                    )
                    WheelPicker(
                        label = "Minutos", values = MinutesValues, selected = customMShown,
                        maxEnabled = if (customH >= MAX_TOTAL_MINUTES / 60) 0 else 59,
                        format = { "$it min" }, onChange = { customM = it }, modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Text(
            when {
                destino == null -> "Nenhum destino escolhido"
                effMinutes < 1 -> "Escolha pelo menos 1 minuto"
                else -> "Destino: ${destino.name}\nDuração total: ${formatMinutes(effMinutes)}"
            },
            color = Color.White, fontSize = 14.sp
        )

        // Pausas: a duração acima já inclui as pausas. O que não cabe fica cinza e não dá para escolher.
        Text("Pausas", color = TituloSecao, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        if (effMinutes < 1) {
            Text("Escolha um destino para programar as pausas.", color = Orbit.Lavender, fontSize = 13.sp)
        } else if (!lim.canPause) {
            Text(
                "Essa missão é curta demais para ter pausas (precisa de pelo menos $MIN_MISSION_FOR_BREAKS min).",
                color = Orbit.Lavender, fontSize = 13.sp
            )
        } else {
            // Ao mexer em uma roda, as outras guardam o valor que está aparecendo (nada volta sozinho depois).
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WheelPicker(
                    label = "Pausas", values = BreaksValues, selected = lim.breaks,
                    maxEnabled = lim.maxBreaks, format = { "$it" },
                    onChange = { wantBreaks = it; wantInterval = lim.interval; wantBreakLen = lim.breakLen },
                    modifier = Modifier.weight(1f)
                )
                WheelPicker(
                    label = "A cada", values = IntervalValues, selected = lim.interval,
                    minEnabled = lim.minInterval, maxEnabled = lim.maxInterval, format = { "$it min" },
                    onChange = { wantInterval = it; wantBreaks = lim.breaks; wantBreakLen = lim.breakLen },
                    modifier = Modifier.weight(1f)
                )
                WheelPicker(
                    label = "Cada pausa", values = BreakLenValues, selected = lim.breakLen,
                    maxEnabled = lim.maxBreakLen, format = { "$it min" },
                    onChange = { wantBreakLen = it; wantBreaks = lim.breaks; wantInterval = lim.interval },
                    modifier = Modifier.weight(1f)
                )
            }
            val pauseMin = lim.breaks * lim.breakLen
            Text(
                if (lim.breaks == 0) "Sem pausas: ${formatMinutes(effMinutes)} de foco direto."
                else {
                    val quantas = if (lim.breaks == 1) "1 pausa" else "${lim.breaks} pausas"
                    "$quantas de ${lim.breakLen} min, uma a cada ${lim.interval} min de foco.\n" +
                            "Foco: ${formatMinutes(effMinutes - pauseMin)} + pausas: ${formatMinutes(pauseMin)} = ${formatMinutes(effMinutes)}"
                },
                color = Orbit.Lavender, fontSize = 13.sp
            )
            Text(
                "O que está em cinza não cabe nesta missão.",
                color = Orbit.Lavender.copy(alpha = 0.6f), fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(4.dp))
        BigButton(
            text = "Decolar",
            enabled = valid,
            onClick = {
                state.startSession(
                    title, (destino ?: planetForMinutes(effMinutes)).name, effMinutes,
                    lim.breaks, lim.interval, lim.breakLen
                )
            }
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PlanetCard(planet: Planet, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Orbit.CardSelecionado else Orbit.CardEscuro)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.height(60.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            PlanetImage(planet, 56.dp)
        }
        Text(planet.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(formatMinutes(planet.defaultMinutes), color = TextoFaixa, fontSize = 11.sp)
    }
}

// ---------------------------------------------------------------- Em voo

private val flightLines = listOf(
    "Motores estáveis. Você está indo bem.",
    "Respire fundo. Um passo de cada vez.",
    "Se a mente fugir, tudo bem: é só voltar para a órbita.",
    "Estou aqui com você, copiloto.",
    "Distração passa. O foguete continua."
)

private val breakLines = listOf(
    "Levante-se, beba uma água e respire.",
    "Alongue o corpo. Eu cuido do foguete.",
    "Descansar também faz parte da viagem."
)

@Composable
private fun RunningView(state: AppState, ses: ActiveSession, onFinished: (Reward) -> Unit) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val paused = ses.pausedAtMs > 0L
    val view = LocalView.current

    // mantém a tela acesa durante a missão (o cronômetro é visual)
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    // O tempo vem do relógio do aparelho, então não "para" se a tela apagar.
    LaunchedEffect(ses.startMs, ses.pausedAtMs) {
        while (true) {
            now = System.currentTimeMillis()
            if (ses.pausedAtMs == 0L && phaseAt(ses, now - ses.startMs).finished) {
                state.completeSession()?.let(onFinished)
                break
            }
            delay(200)
        }
    }
    BackHandler(enabled = !paused) { state.pauseSession() }

    val eff = if (paused) ses.pausedAtMs else now
    val ph = phaseAt(ses, eff - ses.startMs)
    val planet = planetByName(ses.planet)
    // a volta do foguete acompanha o tempo TOTAL (ele segue em órbita durante as pausas): 1 volta = missão completa
    val progress = (ph.elapsedMs.toFloat() / ph.totalMs.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
    val elapsedMin = (ph.focusDoneMs / 60_000L).toInt()
    val line = if (ph.inBreak) breakLines[(ph.breakNumber - 1).coerceAtLeast(0) % breakLines.size]
    else flightLines[elapsedMin % flightLines.size]

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (ph.inBreak) {
            Text("Hora da pausa", color = Orbit.Dourado, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Pausa ${ph.breakNumber} de ${ph.totalBreaks}",
                color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.SemiBold
            )
        } else {
            Text(
                ses.title, color = Color.White, fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.weight(1f))
        OrbitScene(planet, progress, sceneSize = 300.dp)
        Text(
            if (ph.inBreak) "Pausa" else "Tempo de foco",
            color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp
        )
        Text(
            if (ph.inBreak) formatClock(ph.breakLeftMs) else formatClock(ph.focusLeftMs),
            color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Bold
        )
        val extra = when {
            ph.inBreak -> "Foco restante: ${formatClock(ph.focusLeftMs)}"
            ph.nextBreakInMs >= 0 -> "Próxima pausa em ${formatClock(ph.nextBreakInMs)}"
            else -> ""
        }
        Text(extra, color = Orbit.Lavender, fontSize = 14.sp, modifier = Modifier.height(20.dp))
        // com pausas, mostra também quando a missão inteira acaba
        Text(
            if (ph.totalBreaks > 0) "Chegada em ${formatClock(ph.totalMs - ph.elapsedMs)}" else "",
            color = Orbit.Lavender.copy(alpha = 0.7f), fontSize = 13.sp, modifier = Modifier.height(18.dp)
        )
        Spacer(Modifier.weight(1f))
        CatSays(if (ph.inBreak) Mood.Happy else Mood.Focus, line, catWidth = 84.dp)
        Spacer(Modifier.height(10.dp))
        if (ph.inBreak) {
            TextButton(onClick = { state.skipBreak() }) {
                Text("Pular pausa", color = Orbit.Lilas, fontSize = 15.sp)
            }
        }
        Button(
            onClick = { state.pauseSession() },
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Sos)
        ) {
            Text("SOS", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (paused) {
        AlertDialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            containerColor = Orbit.RoxoEscuro,
            titleContentColor = Orbit.Cream,
            textContentColor = Orbit.Cream.copy(alpha = 0.85f),
            icon = { Rocky(Mood.Happy, width = 90.dp) },
            title = { Text("Pausa de emergência") },
            text = {
                Text(
                    "Respire fundo. O tempo está parado e eu estou aqui com você. " +
                            "Se abortar, você fica com o combustível do tempo que já voou, mas não ganha a estrela."
                )
            },
            confirmButton = {
                TextButton(onClick = { state.resumeSession() }) {
                    Text("Continuar", color = Orbit.Lilas, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { state.abortSession()?.let(onFinished) }) {
                    Text("Abortar missão", color = Orbit.Coral, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Planeta no centro e o foguete dando UMA volta lenta na órbita, do começo ao fim da missão.
 * Todos os planetas têm o mesmo tamanho e ficam menores que a órbita: a linha de trás passa
 * por cima do planeta e a da frente por baixo, sem nunca cruzar a imagem.
 */
@Composable
private fun OrbitScene(planet: Planet, progress: Float, sceneSize: Dp) {
    val tilt = -16f
    val rx = sceneSize.value * 0.46f
    val ry = sceneSize.value * 0.19f
    // começa na frente (embaixo), vai para trás do planeta e volta à frente no fim
    val th = (90f + 360f * progress) * PI.toFloat() / 180f
    val tr = tilt * PI.toFloat() / 180f
    val ex = rx * cos(th)
    val ey = ry * sin(th)
    val px = ex * cos(tr) - ey * sin(tr)
    val py = ex * sin(tr) + ey * cos(tr)
    val back = sin(th) < 0f
    val depth = 0.8f + 0.2f * (sin(th) + 1f) / 2f
    val heading = (atan2(ry * cos(th), -rx * sin(th)) * 180f / PI.toFloat()) + 90f + tilt

    Box(Modifier.size(sceneSize), contentAlignment = Alignment.Center) {
        OrbitLine(rx, ry, tilt, back = true)
        if (back) Rocket(px, py, depth, heading)
        PlanetImage(planet, sceneSize * 0.29f)
        OrbitLine(rx, ry, tilt, back = false)
        if (!back) Rocket(px, py, depth, heading)
    }
}

/** Metade da órbita: a de trás (em cima) ou a da frente (embaixo). */
@Composable
private fun OrbitLine(rx: Float, ry: Float, tilt: Float, back: Boolean) {
    Canvas(Modifier.fillMaxSize()) {
        val w = rx.dp.toPx()
        val h = ry.dp.toPx()
        val dash = 5.dp.toPx()
        rotate(tilt, pivot = center) {
            drawArc(
                Orbit.Lavender.copy(alpha = 0.45f),
                startAngle = if (back) 180f else 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - w, center.y - h),
                size = Size(w * 2f, h * 2f),
                style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)))
            )
        }
    }
}

@Composable
private fun Rocket(x: Float, y: Float, depth: Float, heading: Float) {
    Image(
        painterResource(R.drawable.rocket_small),
        contentDescription = "Foguete",
        modifier = Modifier
            .offset(x = x.dp, y = y.dp)
            .size(46.dp)
            .graphicsLayer {
                scaleX = depth; scaleY = depth
                rotationZ = heading
            }
    )
}

// ---------------------------------------------------------------- Fim

@Composable
private fun DoneView(r: Reward, onExit: () -> Unit) {
    BackHandler(onBack = onExit)
    val flown = formatLong(r.seconds)
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
    ) {
        Rocky(if (r.completed) Mood.Party else Mood.Happy, width = 170.dp)
        Text(
            if (r.completed) "Missão cumprida!" else "Missão abortada",
            color = Orbit.Cream, fontSize = 30.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center
        )
        Text(
            when {
                r.completed -> "Você voou $flown sem desviar. Orgulho de você!"
                r.seconds == 0 -> "Você nem chegou a decolar, mas tudo bem. Tentamos de novo quando você quiser."
                else -> "Você voou $flown, e isso conta. Voltar também é coragem."
            },
            color = Orbit.Cream.copy(alpha = 0.9f), fontSize = 16.sp, textAlign = TextAlign.Center
        )
        if (r.fuel > 0) {
            Text("+${r.fuel} de combustível", color = Orbit.Lilas, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
        if (r.star) {
            Text("+1 estrela na sua constelação", color = Orbit.Dourado, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        } else {
            Text(
                "Complete a missão inteira para ganhar todo o combustível e uma estrela.",
                color = Orbit.Lavender, fontSize = 14.sp, textAlign = TextAlign.Center
            )
        }
        if (r.reachedGoal) {
            Text("Meta de hoje cumprida!", color = Orbit.Mint, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        if (r.usedShield) {
            Text("O escudo protegeu a sua sequência de ontem.", color = Orbit.Mint, fontSize = 15.sp, textAlign = TextAlign.Center)
        }
        if (r.streak > 0) {
            Text(
                if (r.streak == 1) "Sequência: 1 dia" else "Sequência: ${r.streak} dias",
                color = Orbit.Laranja, fontSize = 16.sp, fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        BigButton(text = "Voltar à base", onClick = onExit)
    }
}