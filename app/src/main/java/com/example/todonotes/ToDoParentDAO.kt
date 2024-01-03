package com.example.todonotes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoParentDAO {

    @Query("SELECT * FROM parent_todo_items")
    suspend fun getAll(): List<ToDoParentEntity>

    @Insert
    fun insertAll(vararg todo: ToDoParentEntity)

    @Insert(ToDoParentEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(toDoParentEntity: ToDoParentEntity): Long

//    @Query("UPDATE child_todo_items SET isChecked = :isChecked WHERE todoId = :id")
//    suspend fun updateItemCheckedState(id: Int, isChecked: Boolean)

    @Query("DELETE FROM parent_todo_items WHERE parentId = :parentId")
    fun delete(parentId: Int)

    @Query("SELECT * FROM parent_todo_items")
    fun observeAllItems(): Flow<List<ToDoParentEntity>>
}