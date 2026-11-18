package com.rocketorbit.app.data

/*
 * REGRAS DAS PAUSAS (tudo em minutos)
 *
 * A DURAÇÃO da missão é o tempo TOTAL, com as pausas dentro:
 *
 *     duração = foco + pausas
 *
 * Cada pausa vem depois de "intervalo" minutos de foco. Depois da última pausa ainda
 * precisa sobrar pelo menos MIN_LAST_FOCUS minutos de foco (pausa colada no fim não existe):
 *
 *     pausas × (intervalo + pausa) + MIN_LAST_FOCUS  <=  duração
 *
 * E a pausa nunca é maior que o intervalo de foco que vem antes dela.
 * As rodas de escolha usam estes limites para deixar cinza o que não cabe.
 */
const val MAX_BREAKS = 10
const val MAX_INTERVAL = 120
const val MAX_BREAK_LEN = 30
const val MIN_LAST_FOCUS = 5

/** Menor missão que aceita pausa: 5 min de foco + 1 min de pausa + 5 min de foco. */
const val MIN_MISSION_FOR_BREAKS = 5 + 1 + MIN_LAST_FOCUS

// ---------------------------------------------------------------- Escolha (tela Nova missão)

data class PauseLimits(
    val canPause: Boolean,   // a missão é longa o bastante para ter pausa
    val breaks: Int,         // valores já ajustados (sempre válidos)
    val maxBreaks: Int,
    val interval: Int,
    val minInterval: Int,
    val maxInterval: Int,
    val breakLen: Int,
    val maxBreakLen: Int
)

private fun floor5(v: Int): Int = if (v <= 0) 0 else v / 5 * 5
private fun ceil5(v: Int): Int = (maxOf(v, 0) + 4) / 5 * 5

/**
 * Pega o que o usuário pediu e devolve valores que SEMPRE formam uma missão possível,
 * mais o menor/maior valor que cada roda aceita (mantendo as outras duas fixas).
 * O que passar do limite vira o maior valor que cabe.
 *
 * [minutes] é a duração TOTAL da missão.
 */
fun pauseLimits(minutes: Int, wantedBreaks: Int, wantedInterval: Int, wantedBreakLen: Int): PauseLimits {
    if (minutes < MIN_MISSION_FOR_BREAKS) return PauseLimits(false, 0, 0, 5, 5, 5, 1, 1)

    // 1) intervalo: tem que caber pelo menos 1 pausa de 1 min e a última etapa de foco
    val xTop = minOf(MAX_INTERVAL, floor5(minutes - MIN_LAST_FOCUS - 1))
    val x = floor5(wantedInterval.coerceIn(5, xTop))

    // 2) pausa: não passa de 30, nem do intervalo, e ainda sobra a última etapa de foco
    val bTop = minOf(MAX_BREAK_LEN, x, minutes - MIN_LAST_FOCUS - x)
    val b = wantedBreakLen.coerceIn(1, bTop)

    // 3) quantidade de pausas
    val kTop = minOf(MAX_BREAKS, (minutes - MIN_LAST_FOCUS) / (x + b))
    val k = wantedBreaks.coerceIn(0, kTop)

    // limites de cada roda com as outras duas fixas.
    // Com 0 pausas vale como se fosse 1: tudo que aparece liberado aceita pelo menos 1 pausa.
    val kk = maxOf(1, k)
    val room = (minutes - MIN_LAST_FOCUS) / kk        // espaço de (foco + pausa) para cada pausa
    val minInterval = maxOf(5, ceil5(b))
    val maxInterval = minOf(MAX_INTERVAL, floor5(room - b))
    val maxBreakLen = minOf(MAX_BREAK_LEN, x, room - x)

    return PauseLimits(true, k, kTop, x, minInterval, maxInterval, b, maxBreakLen)
}

// ---------------------------------------------------------------- Durante a missão

data class Phase(
    val finished: Boolean,
    val inBreak: Boolean,
    val focusDoneMs: Long,     // foco já voado
    val focusLeftMs: Long,     // foco que ainda falta
    val breakLeftMs: Long,     // quanto falta da pausa atual (0 se não está em pausa)
    val breakNumber: Int,      // pausa atual (1, 2, ...) quando inBreak
    val totalBreaks: Int,
    val nextBreakInMs: Long,   // -1 se não há mais pausas
    val elapsedMs: Long,       // tempo total decorrido (foco + pausas)
    val totalMs: Long          // duração total da missão
)

/** Quantas pausas realmente cabem nesta missão (a duração já inclui as pausas). */
fun effectiveBreaks(ses: ActiveSession): Int {
    val x = ses.intervalMs
    val b = ses.breakMs
    if (ses.breaks <= 0 || x <= 0L || b <= 0L) return 0
    val fit = ((ses.durationMs - 1L) / (x + b)).toInt()
    return minOf(ses.breaks, fit)
}

/** Tempo de foco da missão inteira = duração total menos as pausas. */
fun focusTotalMs(ses: ActiveSession): Long = ses.durationMs - effectiveBreaks(ses) * ses.breakMs

/** Em que ponto da missão estamos, [wallMs] milissegundos depois da decolagem. */
fun phaseAt(ses: ActiveSession, wallMs: Long): Phase {
    val k = effectiveBreaks(ses)
    val x = ses.intervalMs
    val b = ses.breakMs
    val total = ses.durationMs
    val focus = focusTotalMs(ses)

    if (wallMs >= total) {
        return Phase(
            finished = true, inBreak = false, focusDoneMs = focus, focusLeftMs = 0L, breakLeftMs = 0L,
            breakNumber = k, totalBreaks = k, nextBreakInMs = -1L, elapsedMs = total, totalMs = total
        )
    }

    val wall = wallMs.coerceAtLeast(0L)
    var t = wall
    var done = 0L
    for (i in 0 until k) {
        if (t < x) {
            return Phase(
                finished = false, inBreak = false, focusDoneMs = done + t, focusLeftMs = focus - done - t,
                breakLeftMs = 0L, breakNumber = i, totalBreaks = k, nextBreakInMs = x - t,
                elapsedMs = wall, totalMs = total
            )
        }
        t -= x
        done += x
        if (t < b) {
            return Phase(
                finished = false, inBreak = true, focusDoneMs = done, focusLeftMs = focus - done,
                breakLeftMs = b - t, breakNumber = i + 1, totalBreaks = k, nextBreakInMs = -1L,
                elapsedMs = wall, totalMs = total
            )
        }
        t -= b
    }
    done += t
    return Phase(
        finished = false, inBreak = false, focusDoneMs = done, focusLeftMs = focus - done,
        breakLeftMs = 0L, breakNumber = k, totalBreaks = k, nextBreakInMs = -1L,
        elapsedMs = wall, totalMs = total
    )
}