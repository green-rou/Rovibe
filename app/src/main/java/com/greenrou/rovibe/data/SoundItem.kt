package com.greenrou.rovibe.data

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class SoundItem(
    val id: String,
    val name: String,
    val createdAt: LocalDateTime,
    val content: String = "",
) {
    companion object {
        private val nameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

        fun generateName(timestamp: LocalDateTime = LocalDateTime.now()): String =
            "Sound_${timestamp.format(nameFormatter)}"
    }
}
