package com.rocketorbit.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rocketorbit.app.AppState
import com.rocketorbit.app.data.capitalizeName
import com.rocketorbit.app.data.formatDuration
import com.rocketorbit.app.data.formatLong
import com.rocketorbit.app.ui.components.BigButton
import com.rocketorbit.app.ui.components.CatSays
import com.rocketorbit.app.ui.components.InfoRow
import com.rocketorbit.app.ui.components.Mood
import com.rocketorbit.app.ui.components.rememberIsNight
import com.rocketorbit.app.ui.components.StatChip
import com.rocketorbit.app.ui.theme.Orbit

private fun catLine(name: String, todaySeconds: Int, goalSeconds: Int, night: Boolean): Pair<Mood, String> {
    val first = capitalizeName(name).substringBefore(' ')
    val flown = formatLong(todaySeconds)
    return when {
        // de noite o Rocky dorme (zzz), mas acorda se você quiser voar
        night && todaySeconds >= goalSeconds -> Mood.Sleep to "Zzz... Meta de hoje cumprida, $first. Bons sonhos!"
        night && todaySeconds > 0 -> Mood.Sleep to "Zzz... Hoje você já focou por $flown, $first. Descanse. Se quiser voar, eu acordo!"
        night -> Mood.Sleep to "Zzz... Já é tarde, $first. Se quiser decolar, é só me chamar que eu acordo!"
        todaySeconds >= goalSeconds -> Mood.Party to "Meta de hoje cumprida, $first! Você já focou por $flown. Que orgulho!"
        todaySeconds > 0 -> Mood.Happy to "Hoje você já focou por $flown, $first. Mais um voo?"
        else -> Mood.Happy to "Vamos decolar, $first? Cada voo conta."
    }
}

@Composable
fun HomeScreen(state: AppState, onStart: () -> Unit) {
    val user = state.user ?: return
    val stats = state.stats
    val todaySeconds = state.todaySeconds
    val goalSeconds = stats.dailyGoal * 60
    val night = rememberIsNight()
    val (mood, line) = catLine(user.name, todaySeconds, goalSeconds, night)

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip(Icons.Rounded.Star, "${stats.stars}", Orbit.Dourado)
                StatChip(Icons.Rounded.Bolt, "${stats.fuel}", Orbit.Lilas)
                if (stats.shields > 0) StatChip(Icons.Rounded.Shield, "${stats.shields}", Orbit.Mint)
            }

            CatSays(mood, line)

            InfoRow(
                title = "Meta de hoje",
                value = "${formatDuration(todaySeconds)} de ${formatDuration(stats.dailyGoal * 60)}",
                progress = todaySeconds.toFloat() / goalSeconds.coerceAtLeast(1)
            )
        }

        Box(Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp)) {
            BigButton(
                text = if (state.session != null) "Voltar para a missão" else "Iniciar missão",
                onClick = onStart
            )
        }
    }
}