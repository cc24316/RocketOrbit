package com.rocketorbit.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rocketorbit.app.ui.theme.Orbit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive

private val ItemHeight = 40.dp

/**
 * Anima a roda até [index]. Se o usuário pegar a roda no meio da animação, o Compose cancela a animação
 * com uma CancellationException: aqui isso é normal e é engolido, para o efeito que escuta a roda
 * NÃO morrer (era isso que fazia a roda parar de atualizar o valor depois de rolar rápido).
 * Se foi o próprio efeito que foi cancelado (a tela saiu), a exceção continua subindo.
 */
private suspend fun CoroutineScope.animateWheelTo(state: LazyListState, index: Int) {
    try {
        state.animateScrollToItem(index)
    } catch (e: CancellationException) {
        ensureActive()
    }
}

/**
 * Roda de rolagem vertical, igual à do alarme: arrasta e o valor do meio é o escolhido.
 *
 * Valores abaixo de [minEnabled] ou acima de [maxEnabled] ficam cinza. Se o dedo soltar a roda
 * em cima de um valor cinza, ela volta sozinha para o valor permitido mais próximo.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    label: String,
    values: List<Int>,
    selected: Int,
    maxEnabled: Int,
    format: (Int) -> String,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minEnabled: Int = Int.MIN_VALUE
) {
    // A lista tem uma linha vazia em cima e outra embaixo: com 3 linhas visíveis,
    // a linha do meio é sempre values[firstVisibleItemIndex].
    val state = rememberLazyListState(initialFirstVisibleItemIndex = values.indexOf(selected).coerceAtLeast(0))
    val fling = rememberSnapFlingBehavior(state)
    val currentSelected = rememberUpdatedState(selected)
    val currentOnChange = rememberUpdatedState(onChange)
    val itemPx = with(LocalDensity.current) { ItemHeight.toPx() }

    // A roda fica com todo o arrasto: ao chegar no fim dela, a tela de trás não sai rolando junto.
    val keepScroll = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset = available
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
        }
    }

    // Quando o valor muda por fora (ex.: o limite diminuiu), a roda acompanha.
    LaunchedEffect(selected, values) {
        val i = values.indexOf(selected)
        if (i >= 0 && !state.isScrollInProgress && state.firstVisibleItemIndex != i) {
            animateWheelTo(state, i)
        }
    }

    // Quando o dedo solta: confirma o valor (já corrigido para o que é permitido) e encaixa a roda.
    LaunchedEffect(state, values, minEnabled, maxEnabled) {
        snapshotFlow { state.isScrollInProgress }.collect { moving ->
            if (!moving) {
                var target = state.firstVisibleItemIndex.coerceIn(0, values.lastIndex)
                if (state.firstVisibleItemScrollOffset > itemPx / 2f) target = (target + 1).coerceAtMost(values.lastIndex)
                val lo = values.indexOfFirst { it >= minEnabled }
                val hi = values.indexOfLast { it <= maxEnabled }
                if (lo in 0..hi) target = target.coerceIn(lo, hi)

                // avisa na hora (antes da animação), para as outras rodas já atualizarem os limites
                if (values[target] != currentSelected.value) currentOnChange.value(values[target])

                if (state.firstVisibleItemIndex != target || state.firstVisibleItemScrollOffset != 0) {
                    animateWheelTo(state, target)
                }
            }
        }
    }

    // Linha do meio durante o arrasto (para destacar o número).
    val center = (state.firstVisibleItemIndex + if (state.firstVisibleItemScrollOffset > itemPx / 2f) 1 else 0)
        .coerceIn(0, values.lastIndex)

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Orbit.Lavender, fontSize = 13.sp, modifier = Modifier.padding(bottom = 4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(ItemHeight * 3)
                .background(Orbit.CardEscuro, RoundedCornerShape(14.dp))
                .nestedScroll(keepScroll),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(ItemHeight)
                    .padding(horizontal = 6.dp)
                    .background(Color(0x33FFFFFF), RoundedCornerShape(10.dp))
            )
            val rows = listOf<Int?>(null) + values.map { it } + listOf<Int?>(null)
            LazyColumn(state = state, flingBehavior = fling, modifier = Modifier.fillMaxSize()) {
                itemsIndexed(rows) { index, v ->
                    Box(Modifier.fillMaxWidth().height(ItemHeight), contentAlignment = Alignment.Center) {
                        if (v != null) {
                            val isCenter = index - 1 == center
                            Text(
                                format(v),
                                color = when {
                                    v > maxEnabled || v < minEnabled -> Color(0x40FFFFFF)
                                    isCenter -> Color.White
                                    else -> Orbit.Lavender
                                },
                                fontSize = if (isCenter) 20.sp else 16.sp,
                                fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}