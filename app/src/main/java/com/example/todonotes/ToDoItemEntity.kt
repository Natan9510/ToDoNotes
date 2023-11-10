package com.example.todonotes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_table")
data class ToDoItemEntity (
    @PrimaryKey(autoGenerate = true)
    val todoId: Int? = null,
    val toDoText: String,
    val isChecked: Boolean = false
){
}