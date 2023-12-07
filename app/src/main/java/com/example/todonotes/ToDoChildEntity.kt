package com.example.todonotes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_todo_items")
data class ToDoChildEntity (
    @PrimaryKey(autoGenerate = true)
    val todoId: Int? = null,
    val toDoText: String,
    val isChecked: Boolean = false,
    val parentId: Int
){
}