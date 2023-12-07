package com.example.todonotes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parent_todo_items")
data class ToDoParentEntity(
    @PrimaryKey (autoGenerate = true)
    val parentId: Int? = null,
    val title: String
)
