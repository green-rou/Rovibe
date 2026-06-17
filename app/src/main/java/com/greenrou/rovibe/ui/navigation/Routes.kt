package com.greenrou.rovibe.ui.navigation

object Routes {
    const val SOUNDS_GRAPH = "sounds_graph"
    const val COMPOSITIONS_GRAPH = "compositions_graph"

    const val SOUNDS = "sounds"
    const val COMPOSITIONS = "compositions"
    const val SETTINGS = "settings"

    const val ITEM_ID_ARG = "itemId"
    const val CREATE = "create"
    const val CREATE_PATTERN = "create?itemId={itemId}"

    const val COMP_ID_ARG = "id"
    const val COMPOSITION_EDITOR = "composition_editor"
    const val COMPOSITION_EDITOR_PATTERN = "composition_editor?id={id}"

    fun create(itemId: String? = null): String =
        if (itemId != null) "create?itemId=$itemId" else CREATE

    fun compositionEditor(id: String): String = "composition_editor?id=$id"
}
