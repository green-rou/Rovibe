package com.greenrou.rovibe.ui.screen.compositions

import com.greenrou.rovibe.data.composition.Composition

data class CompositionsState(
    val items: List<Composition> = emptyList(),
)
