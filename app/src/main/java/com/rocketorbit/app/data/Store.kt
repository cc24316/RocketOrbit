package com.rocketorbit.app.data

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class User(val name: String, val email: String, val salt: String, val hash: String)

data class ActiveSession(
    val title: String,
    val planet: String,
    val startMs: Long,
    val durationMs: Long,          // tempo TOTAL da missão (foco + pausas)
    val breaks: Int = 0,           // quantas pausas o usuário programou
    val intervalMs: Long = 0L,     // de quanto em quanto tempo de foco
    val breakMs: Long = 0L,        // duração de cada pausa
    val pausedAtMs: Long = 0L      // != 0 quando o SOS está ativo
)

data class Stats(
    val fuel: Int = 0,
    val stars: Int = 0,              // constelação: 1 estrela por missão completa
    val totalSeconds: Int = 0,
    val missions: Int = 0,
    val streak: Int = 0,
    val lastDay: Long = -1L,
    val todaySeconds: Int = 0,
    val todayDay: Long = -1L,
    val dailyGoal: Int = 30,         // minutos
    val shields: Int = 0
)

object Security {
    fun newSalt(): ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }

    fun hash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 60_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    fun decode(text: String): ByteArray = Base64.decode(text, Base64.NO_WRAP)

    fun matches(password: String, user: User): Boolean {
        val expected = decode(user.hash)
        val actual = hash(password, decode(user.salt))
        return MessageDigest.isEqual(expected, actual)
    }
}

/**
 * Tudo fica no próprio celular (SharedPreferences). Sem internet, sem servidor.
 * Quando quisermos nuvem (Firebase), é só trocar esta classe.
 */
class Store(context: Context) {
    private val p = context.applicationContext
        .getSharedPreferences("rocketorbit", Context.MODE_PRIVATE)

    fun users(): List<User> {
        val arr = JSONArray(p.getString("users", "[]"))
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            User(o.getString("name"), o.getString("email"), o.getString("salt"), o.getString("hash"))
        }
    }

    fun addUser(u: User) {
        val arr = JSONArray(p.getString("users", "[]"))
        arr.put(
            JSONObject()
                .put("name", u.name).put("email", u.email)
                .put("salt", u.salt).put("hash", u.hash)
        )
        p.edit { putString("users", arr.toString()) }
    }

    fun userByEmail(email: String): User? = users().firstOrNull { it.email == email }

    var sessionEmail: String?
        get() = p.getString("session", null)
        set(value) {
            p.edit { if (value == null) remove("session") else putString("session", value) }
        }

    fun loadStats(email: String): Stats {
        val raw = p.getString("stats_$email", null) ?: return Stats()
        return try {
            val o = JSONObject(raw)
            Stats(
                fuel = o.optInt("fuel", 0),
                stars = o.optInt("stars", 0),
                // versões antigas guardavam em minutos
                totalSeconds = o.optInt("totalSeconds", o.optInt("totalMinutes", 0) * 60),
                missions = o.optInt("missions", 0),
                streak = o.optInt("streak", 0),
                lastDay = o.optLong("lastDay", -1L),
                todaySeconds = o.optInt("todaySeconds", o.optInt("todayMinutes", 0) * 60),
                todayDay = o.optLong("todayDay", -1L),
                dailyGoal = o.optInt("dailyGoal", 30),
                shields = o.optInt("shields", 0)
            )
        } catch (e: Exception) {
            Stats()
        }
    }

    fun saveStats(email: String, s: Stats) {
        val o = JSONObject()
            .put("fuel", s.fuel).put("stars", s.stars)
            .put("totalSeconds", s.totalSeconds).put("missions", s.missions)
            .put("streak", s.streak).put("lastDay", s.lastDay)
            .put("todaySeconds", s.todaySeconds).put("todayDay", s.todayDay)
            .put("dailyGoal", s.dailyGoal).put("shields", s.shields)
        p.edit { putString("stats_$email", o.toString()) }
    }

    fun loadSession(): ActiveSession? {
        val start = p.getLong("ses_start", -1L)
        if (start < 0) return null
        var breaks = p.getInt("ses_breaks", 0)
        val interval = p.getLong("ses_interval", 0L)
        val breakMs = p.getLong("ses_break_ms", 0L)
        var duration = p.getLong("ses_duration", 0L)
        // Missão salva por uma versão antiga: a duração era só o foco. Agora é o total (foco + pausas).
        if (p.getInt("ses_v", 1) < 2) {
            val k = if (breaks > 0 && interval > 0L && breakMs > 0L) minOf(breaks.toLong(), (duration - 1L) / interval).toInt() else 0
            breaks = k
            duration += k * breakMs
        }
        return ActiveSession(
            title = p.getString("ses_title", "Missão de foco") ?: "Missão de foco",
            planet = p.getString("ses_planet", "Lua") ?: "Lua",
            startMs = start,
            durationMs = duration,
            breaks = breaks,
            intervalMs = interval,
            breakMs = breakMs,
            pausedAtMs = p.getLong("ses_paused", 0L)
        )
    }

    fun saveSession(s: ActiveSession?) {
        p.edit {
            if (s == null) {
                remove("ses_start"); remove("ses_title"); remove("ses_duration"); remove("ses_planet")
                remove("ses_breaks"); remove("ses_interval"); remove("ses_break_ms"); remove("ses_paused"); remove("ses_v")
            } else {
                putLong("ses_start", s.startMs)
                putString("ses_title", s.title)
                putString("ses_planet", s.planet)
                putLong("ses_duration", s.durationMs)
                putInt("ses_breaks", s.breaks)
                putLong("ses_interval", s.intervalMs)
                putLong("ses_break_ms", s.breakMs)
                putLong("ses_paused", s.pausedAtMs)
                putInt("ses_v", 2)
            }
        }
    }
}