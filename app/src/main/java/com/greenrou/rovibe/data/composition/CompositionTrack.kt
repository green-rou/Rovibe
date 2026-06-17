package com.greenrou.rovibe.data.composition

data class CompositionTrack(
    val id: String,
    val name: String,
    val patterns: List<PatternBlock> = emptyList(),
)
