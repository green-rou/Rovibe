package com.greenrou.rovibe.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SoundRepository(private val store: SoundItemStore) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _items = MutableStateFlow(store.loadAll())
    val items: StateFlow<List<SoundItem>> = _items.asStateFlow()

    fun add(item: SoundItem) {
        _items.update { listOf(item) + it }
        scope.launch { store.save(item) }
    }

    fun update(item: SoundItem) {
        _items.update { list -> list.map { if (it.id == item.id) item else it } }
        scope.launch { store.save(item) }
    }

    fun rename(id: String, name: String) {
        val updated = _items.value.find { it.id == id }?.copy(name = name) ?: return
        _items.update { list -> list.map { if (it.id == id) updated else it } }
        scope.launch { store.save(updated) }
    }

    fun delete(id: String) {
        _items.update { it.filter { item -> item.id != id } }
        scope.launch { store.delete(id) }
    }

    fun getById(id: String): SoundItem? = _items.value.find { it.id == id }
}
