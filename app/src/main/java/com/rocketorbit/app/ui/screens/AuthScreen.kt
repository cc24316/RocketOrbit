package com.rocketorbit.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rocketorbit.app.AppState
import com.rocketorbit.app.R
import com.rocketorbit.app.ui.components.BigButton
import com.rocketorbit.app.ui.components.CatSays
import com.rocketorbit.app.ui.components.ChoiceChip
import com.rocketorbit.app.ui.components.GradientTitle
import com.rocketorbit.app.ui.components.Mood
import com.rocketorbit.app.ui.theme.Orbit

@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Orbit.Cream,
    unfocusedTextColor = Orbit.Cream,
    focusedBorderColor = Orbit.Lilas,
    unfocusedBorderColor = Orbit.GlassLine,
    cursorColor = Orbit.Lilas,
    focusedLabelColor = Orbit.Lilas,
    unfocusedLabelColor = Orbit.Lavender,
    focusedContainerColor = Orbit.Glass,
    unfocusedContainerColor = Orbit.Glass
)

@Composable
fun AuthScreen(state: AppState) {
    var registering by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Image(
            painterResource(R.drawable.logo_rocketorbit),
            contentDescription = "RocketOrbit",
            modifier = Modifier.height(150.dp)
        )
        GradientTitle("RocketOrbit", size = 40)
        Spacer(Modifier.height(16.dp))

        CatSays(
            mood = if (error != null) Mood.Worried else Mood.Happy,
            text = error ?: if (registering)
                "Olá! Eu sou o Rocky. Vou ser o seu copiloto. Qual é o seu nome?"
            else
                "Que bom ver você de novo! Vamos decolar?",
            catWidth = 96.dp
        )
        Spacer(Modifier.height(18.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ChoiceChip("Entrar", !registering, { registering = false; error = null }, Modifier.weight(1f))
            ChoiceChip("Criar conta", registering, { registering = true; error = null }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))

        if (registering) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Como posso chamar você?") },
                singleLine = true, shape = RoundedCornerShape(18.dp),
                colors = fieldColors(), modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
        }
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("E-mail") },
            singleLine = true, shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = fieldColors(), modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Senha") },
            singleLine = true, shape = RoundedCornerShape(18.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = fieldColors(), modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))

        BigButton(
            text = if (registering) "Criar conta e decolar" else "Entrar",
            onClick = {
                error = if (registering) state.register(name, email, password)
                else state.login(email, password)
            }
        )
        Spacer(Modifier.height(24.dp))
    }
}
