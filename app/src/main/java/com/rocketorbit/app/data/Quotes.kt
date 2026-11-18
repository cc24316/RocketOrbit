package com.rocketorbit.app.data

data class Quote(val text: String, val author: String)

object Quotes {
    private val all = listOf(
        Quote("Não se limite. Muitas pessoas limitam-se ao que acham que podem fazer. Você pode ir tão longe quanto sua mente permitir.", "Mary Kay Ash"),
        Quote("O seu começo parecerá modesto, mas o seu futuro será de grande prosperidade.", "Jó 8:7 (Bíblia)"),
        Quote("Nada na vida deve ser temido, somente compreendido. Agora é hora de compreender mais, para que possamos temer menos.", "Marie Curie"),
        Quote("O começo é a parte mais importante do trabalho.", "Platão"),
        Quote("Não há necessidade de pressa. Não há necessidade de brilhar. Não há necessidade de ser ninguém além de si mesmo.", "Virginia Woolf"),
        Quote("Não é porque as coisas são difíceis que não ousamos; é porque não ousamos que elas são difíceis.", "Sêneca"),
        Quote("Se você não pode voar, corra. Se não pode correr, caminhe. Se não pode caminhar, rasteje, mas continue em frente de qualquer jeito.", "Mary McLeod Bethune"),
        Quote("A nossa maior glória não reside em jamais cair, mas em nos levantar cada vez que caímos.", "Confúcio"),
        Quote("O que vale na vida não é o ponto de partida e sim a caminhada. Caminhando e semeando, no fim, terás o que colher.", "Cora Coralina"),
        Quote("Se você quer transformar o mundo, experimente primeiro promover o seu aperfeiçoamento pessoal.", "Sócrates"),
        Quote("É que tem mais chão nos meus olhos do que cansaço nas minhas pernas, mais esperança nos meus passos do que tristeza nos meus ombros.", "Cora Coralina"),
        Quote("A energia da mente é a essência da vida.", "Aristóteles")
    )

    private var last = -1

    /** Uma frase diferente da anterior a cada vez que o Perfil é aberto. */
    fun next(): Quote {
        var i: Int
        do {
            i = all.indices.random()
        } while (i == last)
        last = i
        return all[i]
    }
}
