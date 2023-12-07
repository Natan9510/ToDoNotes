package com.example.todonotes

sealed class ToDoBaseListItem {

    data class ToDoListItem(val id: Int, val title: String? = null) : ToDoBaseListItem()
}
