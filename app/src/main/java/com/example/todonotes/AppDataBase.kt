package com.example.todonotes

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ToDoChildEntity::class, ToDoParentEntity::class], version = 1)
abstract class AppDataBase: RoomDatabase() {
    abstract val toDoChildDao: ToDoChildDAO
    abstract val toDoParentDao: ToDoParentDAO
}