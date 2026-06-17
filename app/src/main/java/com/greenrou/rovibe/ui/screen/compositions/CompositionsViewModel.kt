package com.greenrou.rovibe.ui.screen.compositions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rovibe.data.composition.Composition
import com.greenrou.rovibe.data.composition.CompositionRepository
import com.greenrou.rovibe.data.composition.generateCompositionName
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class CompositionsViewModel(
    private val repository: CompositionRepository,
) : ViewModel() {

    val state: StateFlow<CompositionsState> = repository.items
        .map { CompositionsState(items = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompositionsState())

    fun createNew(): String {
        val composition = Composition(
            id = UUID.randomUUID().toString(),
            name = generateCompositionName(),
        )
        repository.create(composition)
        return composition.id
    }

    fun delete(id: String) {
        repository.delete(id)
    }
}
