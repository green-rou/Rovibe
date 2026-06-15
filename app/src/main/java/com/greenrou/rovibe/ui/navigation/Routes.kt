package com.greenrou.rovibe.ui.navigation

object Routes {
    const val HOME = "home"
    const val ITEM_ID_ARG = "itemId"
    const val CREATE = "create"
    const val CREATE_PATTERN = "create?itemId={itemId}"

    fun create(itemId: String? = null): String =
        if (itemId != null) "create?itemId=$itemId" else CREATE
}
