package com.greenrou.rovibe.data.composition

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CompositionRepository(private val store: CompositionStore) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _items = MutableStateFlow(store.loadAll())
    val items: StateFlow<List<Composition>> = _items.asStateFlow()

    fun create(composition: Composition) {
        _items.update { listOf(composition) + it }
        scope.launch { store.save(composition) }
    }

    fun update(composition: Composition) {
        _items.update { list -> list.map { if (it.id == composition.id) composition else it } }
        scope.launch { store.save(composition) }
    }

    fun delete(id: String) {
        _items.update { it.filter { c -> c.id != id } }
        scope.launch { store.delete(id) }
    }

    fun getById(id: String): Composition? = _items.value.find { it.id == id }
}
