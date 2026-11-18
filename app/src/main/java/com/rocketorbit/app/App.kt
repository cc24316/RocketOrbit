package com.rocketorbit.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.rocketorbit.app.ui.components.StarField
import com.rocketorbit.app.ui.screens.AuthScreen
import com.rocketorbit.app.ui.screens.FocusScreen
import com.rocketorbit.app.ui.screens.HomeScreen
import com.rocketorbit.app.ui.screens.ProfileScreen
import com.rocketorbit.app.ui.theme.Orbit
import com.rocketorbit.app.ui.theme.RocketOrbitTheme

enum class Tab(val label: String, val icon: ImageVector) {
    Home("Início", Icons.Rounded.Home),
    Profile("Perfil", Icons.Rounded.Person),
    Focus("Foco", Icons.Rounded.Home)
}

@Composable
fun RocketOrbitApp(state: AppState) {
    RocketOrbitTheme {
        Box(Modifier.fillMaxSize()) {
            StarField(Modifier.fillMaxSize())
            if (state.user == null) AuthScreen(state) else MainShell(state)
        }
    }
}

@Composable
private fun MainShell(state: AppState) {
    // Se havia uma missão em andamento (app fechado ou tela bloqueada), volta direto pra ela.
    var tab by rememberSaveable { mutableStateOf(if (state.session != null) Tab.Focus else Tab.Home) }

    if (tab == Tab.Focus) {
        FocusScreen(state, onExit = { tab = Tab.Home })
        return
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(containerColor = Orbit.RoxoEscuro.copy(alpha = 0.92f)) {
                listOf(Tab.Home, Tab.Profile).forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Orbit.Cream,
                            selectedTextColor = Orbit.Lilas,
                            indicatorColor = Orbit.Roxo,
                            unselectedIconColor = Orbit.Lavender,
                            unselectedTextColor = Orbit.Lavender
                        )
                    )
                }
            }
        }
    ) { inner ->
        Box(Modifier.padding(inner)) {
            when (tab) {
                Tab.Home -> HomeScreen(state, onStart = { tab = Tab.Focus })
                Tab.Profile -> ProfileScreen(state)
                Tab.Focus -> Unit
            }
        }
    }
}