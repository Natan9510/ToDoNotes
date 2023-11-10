package com.example.todonotes

sealed class ToDoBaseListItem {

    data class ToDoListItem (val id: Int, val text: String? = null, val isChecked: Boolean = false):
            ToDoBaseListItem()

    data class CheckedItemsHeader(val text: String): ToDoBaseListItem()

    object AddToDoItemButton: ToDoBaseListItem()

}