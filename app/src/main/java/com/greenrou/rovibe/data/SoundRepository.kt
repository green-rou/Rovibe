package com.greenrou.rovibe.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SoundRepository {

    private val _items = MutableStateFlow<List<SoundItem>>(emptyList())
    val items: StateFlow<List<SoundItem>> = _items.asStateFlow()

    fun add(item: SoundItem) {
        _items.update { listOf(item) + it }
    }

    fun update(item: SoundItem) {
        _items.update { list -> list.map { if (it.id == item.id) item else it } }
    }

    fun rename(id: String, name: String) {
        _items.update { list -> list.map { if (it.id == id) it.copy(name = name) else it } }
    }

    fun getById(id: String): SoundItem? = _items.value.find { it.id == id }
}
