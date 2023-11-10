package com.example.todonotes

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ToDoItemEntity::class], version = 1)
abstract class AppDataBase: RoomDatabase() {
    abstract val toDoDao: ToDoDAO
}