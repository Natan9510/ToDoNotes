package com.example.todonotes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoChildDAO {

    @Query("SELECT * FROM child_todo_items")
    suspend fun getAll(): List<ToDoChildEntity>

    @Query("SELECT * FROM child_todo_items")
    fun observeAllItems(): Flow<List<ToDoChildEntity>>

    @Query("SELECT * FROM child_todo_items WHERE parentId = :parentID")
    fun observeItemsForOneParent(parentID: Int): Flow<List<ToDoChildEntity>>

    @Insert
    fun insertAll(vararg todo: ToDoChildEntity)

    @Insert(ToDoChildEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(toDoItemEntity: ToDoChildEntity)

    @Query("UPDATE child_todo_items SET isChecked = :isChecked WHERE todoId = :id")
    suspend fun updateItemCheckedState(id: Int, isChecked: Boolean)

    @Query("DELETE FROM child_todo_items WHERE todoId = :id")
    fun deleteByClickingOnToDoItem(id: Int)

    @Query("DELETE FROM child_todo_items WHERE parentId = :parentId")
    fun deleteByLongClickOnList(parentId: Int)
}