package com.rocketorbit.app.data

import com.rocketorbit.app.R

/**
 * Destino da missão. As imagens são as do primeiro projeto, todas recortadas
 * para o mesmo tamanho. Saturno e Urano têm anel, então usam [scale] maior
 * para a esfera ficar do mesmo tamanho dos outros.
 */
data class Planet(
    val name: String,
    val fromMinutes: Int,      // a partir de quantos minutos este destino serve para o tempo personalizado
    val defaultMinutes: Int,   // duração da missão ao escolher o planeta
    val image: Int,
    val scale: Float = 1f
)

val Planets = listOf(
    Planet("Lua", 10, 15, R.drawable.planeta_lua),
    Planet("Vênus", 20, 30, R.drawable.planeta_venus),
    Planet("Marte", 30, 45, R.drawable.planeta_marte),
    Planet("Mercúrio", 45, 60, R.drawable.planeta_mercurio),
    Planet("Júpiter", 60, 75, R.drawable.planeta_jupiter),
    Planet("Saturno", 90, 105, R.drawable.planeta_saturno, scale = 1.5f),
    Planet("Urano", 120, 135, R.drawable.planeta_urano, scale = 1.5f),
    Planet("Netuno", 150, 165, R.drawable.planeta_netuno),
    Planet("Plutão", 180, 180, R.drawable.planeta_plutao)
)

fun planetByName(name: String): Planet = Planets.firstOrNull { it.name == name } ?: Planets.first()

/** Tempo personalizado: o destino acompanha a duração escolhida. */
fun planetForMinutes(minutes: Int): Planet =
    Planets.lastOrNull { it.fromMinutes <= minutes } ?: Planets.first()
