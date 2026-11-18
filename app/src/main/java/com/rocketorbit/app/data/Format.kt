package com.rocketorbit.app.data

/** 45 -> "45 min", 60 -> "1h", 75 -> "1h 15min". */
fun formatMinutes(minutes: Int): String = when {
    minutes < 60 -> "$minutes min"
    minutes % 60 == 0 -> "${minutes / 60}h"
    else -> "${minutes / 60}h ${minutes % 60}min"
}

/**
 * Tempo exato, compacto, com horas, minutos e segundos (some o que for zero):
 * 0 -> "0 s", 45 -> "45 s", 259 -> "4 min 19 s", 3900 -> "1 h 5 min", 3905 -> "1 h 5 min 5 s".
 */
fun formatDuration(seconds: Int): String {
    val total = seconds.coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    val parts = buildList {
        if (h > 0) add("$h h")
        if (m > 0) add("$m min")
        if (s > 0) add("$s s")
    }
    return if (parts.isEmpty()) "0 s" else parts.joinToString(" ")
}

/**
 * Por extenso, para frases (sempre com horas, minutos e segundos que existirem):
 * "19 segundos", "1 minuto e 5 segundos", "1 hora, 4 minutos e 19 segundos".
 */
fun formatLong(seconds: Int): String {
    val total = seconds.coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    val parts = buildList {
        if (h > 0) add(if (h == 1) "1 hora" else "$h horas")
        if (m > 0) add(if (m == 1) "1 minuto" else "$m minutos")
        if (s > 0) add(if (s == 1) "1 segundo" else "$s segundos")
    }
    return when (parts.size) {
        0 -> "0 segundos"
        1 -> parts[0]
        2 -> "${parts[0]} e ${parts[1]}"
        else -> "${parts[0]}, ${parts[1]} e ${parts[2]}"
    }
}

/** "lucas silva" -> "Lucas Silva". */
fun capitalizeName(name: String): String =
    name.trim().split(" ").filter { it.isNotEmpty() }
        .joinToString(" ") { w -> w.lowercase().replaceFirstChar { it.uppercase() } }

/** Relógio: mm:ss, ou h:mm:ss a partir de 1 hora. */
fun formatClock(ms: Long): String {
    val total = ((ms.coerceAtLeast(0L) + 999L) / 1000L).toInt()
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}