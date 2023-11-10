package com.example.todonotes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoDAO {

    @Query("SELECT * FROM todo_table")
    suspend fun getAll(): List<ToDoItemEntity>

    @Query("SELECT * FROM todo_table")
    fun observeAllItems(): Flow<List<ToDoItemEntity>>

    @Insert
    fun insertAll(vararg todo: ToDoItemEntity)

    @Insert(ToDoItemEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(toDoItemEntity: ToDoItemEntity)

    @Query("UPDATE todo_table SET isChecked = :isChecked WHERE todoId = :id")
    suspend fun updateItemCheckedState(id: Int, isChecked: Boolean)

    @Query("DELETE FROM TODO_TABLE")
    fun delete()
}