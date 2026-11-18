package com.rocketorbit.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rocketorbit.app.data.ActiveSession
import com.rocketorbit.app.data.Security
import com.rocketorbit.app.data.Stats
import com.rocketorbit.app.data.Store
import com.rocketorbit.app.data.User
import com.rocketorbit.app.data.capitalizeName
import com.rocketorbit.app.data.focusTotalMs
import com.rocketorbit.app.data.phaseAt
import java.time.LocalDate

/** O que o jogador ganhou ao terminar (ou abortar) uma missão. */
data class Reward(
    val completed: Boolean,
    val seconds: Int,          // tempo de foco realmente voado
    val fuel: Int,
    val star: Boolean,         // +1 estrela na constelação (só missão completa)
    val streak: Int,
    val usedShield: Boolean,
    val reachedGoal: Boolean
)

class AppState(private val store: Store) {

    var user by mutableStateOf(store.sessionEmail?.let { store.userByEmail(it) })
        private set
    var stats by mutableStateOf(user?.let { store.loadStats(it.email) } ?: Stats())
        private set
    var session by mutableStateOf(store.loadSession())
        private set

    private fun today(): Long = LocalDate.now().toEpochDay()

    /** Segundos de foco de hoje (zera sozinho quando vira o dia). */
    val todaySeconds: Int
        get() = if (stats.todayDay == today()) stats.todaySeconds else 0

    // ---------- Conta (local) ----------

    /** Retorna uma mensagem de erro, ou null se deu certo. */
    fun register(name: String, email: String, password: String): String? {
        val n = capitalizeName(name)
        val e = email.trim().lowercase()
        if (n.isEmpty()) return "Como o Rocky pode chamar você?"
        if (!e.contains("@") || !e.contains(".")) return "Esse e-mail parece inválido. Confira, por favor."
        if (password.length < 6) return "A senha precisa ter pelo menos 6 caracteres."
        if (store.userByEmail(e) != null) return "Esse e-mail já tem uma conta. Que tal entrar?"
        val salt = Security.newSalt()
        val u = User(n, e, Security.encode(salt), Security.encode(Security.hash(password, salt)))
        store.addUser(u)
        enter(u)
        return null
    }

    fun login(email: String, password: String): String? {
        val e = email.trim().lowercase()
        val u = store.userByEmail(e) ?: return "Esse e-mail ainda não tem conta. Que tal criar uma?"
        if (!Security.matches(password, u)) return "Senha incorreta. Tente de novo, sem pressa."
        enter(u)
        return null
    }

    private fun enter(u: User) {
        store.sessionEmail = u.email
        user = u
        stats = store.loadStats(u.email)
    }

    fun logout() {
        store.sessionEmail = null
        store.saveSession(null)
        session = null
        user = null
        stats = Stats()
    }

    // ---------- Missões ----------

    /** [minutes] é o tempo TOTAL da missão, com as pausas dentro. */
    fun startSession(
        title: String, planet: String, minutes: Int,
        breaks: Int, intervalMin: Int, breakMin: Int
    ) {
        val s = ActiveSession(
            title = title.trim().ifEmpty { "Missão de foco" },
            planet = planet,
            startMs = System.currentTimeMillis(),
            durationMs = minutes * 60_000L,
            breaks = breaks,
            intervalMs = intervalMin * 60_000L,
            breakMs = breakMin * 60_000L
        )
        store.saveSession(s)
        session = s
    }

    /** SOS: para o relógio na hora. */
    fun pauseSession() {
        val s = session ?: return
        if (s.pausedAtMs > 0L) return
        val ns = s.copy(pausedAtMs = System.currentTimeMillis())
        store.saveSession(ns)
        session = ns
    }

    /** Volta do SOS: o tempo parado não é descontado da missão. */
    fun resumeSession() {
        val s = session ?: return
        if (s.pausedAtMs <= 0L) return
        val ns = s.copy(startMs = s.startMs + (System.currentTimeMillis() - s.pausedAtMs), pausedAtMs = 0L)
        store.saveSession(ns)
        session = ns
    }

    /** Encerra a pausa programada antes da hora. */
    fun skipBreak() {
        val s = session ?: return
        if (s.pausedAtMs > 0L) return
        val ph = phaseAt(s, System.currentTimeMillis() - s.startMs)
        if (!ph.inBreak) return
        val ns = s.copy(startMs = s.startMs - ph.breakLeftMs)
        store.saveSession(ns)
        session = ns
    }

    /** Missão chegou ao fim: combustível total da missão + 1 estrela. */
    fun completeSession(): Reward? {
        val s = session ?: return null
        // só o foco conta (as pausas não rendem combustível nem entram no tempo de foco)
        val seconds = (focusTotalMs(s) / 1000L).toInt()
        val reward = record(seconds, completed = true, plannedSeconds = seconds)
        store.saveSession(null)
        session = null
        return reward
    }

    /** Abortar: o foco já voado conta e rende combustível proporcional (sem estrela). */
    fun abortSession(): Reward? {
        val s = session ?: return null
        val now = if (s.pausedAtMs > 0L) s.pausedAtMs else System.currentTimeMillis()
        val flownSeconds = (phaseAt(s, now - s.startMs).focusDoneMs / 1000L).toInt()
        val reward = record(flownSeconds, completed = false, plannedSeconds = (focusTotalMs(s) / 1000L).toInt())
        store.saveSession(null)
        session = null
        return reward
    }

    /**
     * Combustível: 2 por minuto de foco (1 a cada 30 s).
     * Missão completa rende o total planejado + 1 estrela; abortada rende só o tempo voado.
     */
    private fun record(seconds: Int, completed: Boolean, plannedSeconds: Int): Reward? {
        val u = user ?: return null
        val s = stats
        val day = today()

        val before = if (s.todayDay == day) s.todaySeconds else 0
        val after = before + seconds
        val goalSeconds = s.dailyGoal * 60
        val reachedGoal = before < goalSeconds && after >= goalSeconds

        var streak = s.streak
        var shields = s.shields
        var lastDay = s.lastDay
        var usedShield = false

        // Só conta para a sequência se foi uma missão de verdade (completa ou com 5 min ou mais).
        if (completed || seconds >= 300) {
            when {
                s.lastDay == day -> Unit
                s.lastDay == day - 1 -> streak += 1
                s.lastDay == day - 2 && shields > 0 -> {
                    shields -= 1; streak += 1; usedShield = true
                }
                else -> streak = 1
            }
            if (s.lastDay != day && streak % 7 == 0 && shields < 2) shields += 1
            lastDay = day
        }

        val fuel = (if (completed) plannedSeconds else seconds) / 30

        stats = s.copy(
            fuel = s.fuel + fuel,
            stars = s.stars + if (completed) 1 else 0,
            totalSeconds = s.totalSeconds + seconds,
            missions = s.missions + if (completed) 1 else 0,
            streak = streak,
            lastDay = lastDay,
            todaySeconds = after,
            todayDay = day,
            shields = shields
        )
        store.saveStats(u.email, stats)

        return Reward(completed, seconds, fuel, completed, streak, usedShield, reachedGoal)
    }
}