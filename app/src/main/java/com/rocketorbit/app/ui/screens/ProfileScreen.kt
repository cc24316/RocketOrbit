package com.rocketorbit.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rocketorbit.app.AppState
import com.rocketorbit.app.data.Quotes
import com.rocketorbit.app.data.capitalizeName
import com.rocketorbit.app.data.formatDuration
import com.rocketorbit.app.ui.components.Rocky
import com.rocketorbit.app.ui.components.Mood
import com.rocketorbit.app.ui.components.rememberIsNight
import com.rocketorbit.app.ui.theme.Orbit

@Composable
fun ProfileScreen(state: AppState) {
    val user = state.user ?: return
    val stats = state.stats
    // Uma frase nova a cada vez que o Perfil é aberto (a tela sai da composição ao trocar de aba).
    val quote = remember { Quotes.next() }
    val cardShape = RoundedCornerShape(16.dp)
    val night = rememberIsNight()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Rocky(if (night) Mood.Sleep else Mood.Happy, width = 120.dp)
        Text(capitalizeName(user.name), color = Orbit.Cream, fontSize = 26.sp, fontWeight = FontWeight.Black)
        Text(user.email, color = Orbit.Lavender, fontSize = 14.sp)

        Column(
            Modifier
                .fillMaxWidth()
                .background(Orbit.Card, cardShape)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Seu progresso", color = Orbit.Lavender, fontSize = 14.sp)
            Text(if (stats.missions == 1) "1 missão concluída" else "${stats.missions} missões concluídas", color = Orbit.Cream, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            // tempo real de foco, sem as pausas: horas, minutos e segundos
            Text("${formatDuration(stats.totalSeconds)} de foco no total", color = Orbit.Cream, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Hoje: ${formatDuration(state.todaySeconds)} de foco", color = Orbit.Lavender, fontSize = 14.sp)
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(Orbit.Card, cardShape)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "“${quote.text}”",
                color = Orbit.Cream, fontSize = 17.sp, lineHeight = 24.sp, fontStyle = FontStyle.Italic
            )
            Text("– ${quote.author}", color = Orbit.Lilas, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(56.dp))
        TextButton(onClick = { state.logout() }) {
            Text("Sair da conta", color = Orbit.Coral, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}