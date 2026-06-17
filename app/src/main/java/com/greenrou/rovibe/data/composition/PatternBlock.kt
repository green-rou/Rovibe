package com.greenrou.rovibe.data.composition

data class PatternBlock(
    val id: String,
    val soundId: String,
    val startBar: Float,
    val durationBars: Float = 1f,
)
