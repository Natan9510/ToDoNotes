package com.example.todonotes

sealed class ToDoBaseItem {

    data class ToDoItem (val id: Int, val text: String? = null, val isChecked: Boolean = false):
            ToDoBaseItem()

    data class CheckedItemsHeader(val text: String): ToDoBaseItem()

    object AddToDoItemButton: ToDoBaseItem()

}