package com.example.todonotes

sealed class ToDoBaseListItem {

    data class ToDoListItem(val id: Int? = null, val title: String? = null) : ToDoBaseListItem()
}
