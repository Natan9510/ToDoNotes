package com.example.todonotes

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    val TAG: String = "zlo"

    lateinit var recyclerView: RecyclerView
    lateinit var saveButton: Button
    lateinit var titleEditText: EditText

    var myList: MutableList<ToDoBaseItem> = mutableListOf()

    var toDoAdapter: ToDoAdapter = ToDoAdapter(deleteItemCallback = { id -> deleteItem(id)},
        itemCheckedCallback = { id, isChecked -> updateCheckState(id, isChecked) },
        addItemCallback = { addItem() },
        updateTextCallback = {id, text -> updateText(id, text)})

    lateinit var appDataBase: AppDataBase
    lateinit var taskChildDao: ToDoChildDAO
    lateinit var taskParentDAO: ToDoParentDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: MainActivity")
        setContentView(R.layout.activity_main)

        saveButton = findViewById(R.id.save_button)
        titleEditText = findViewById(R.id.title_edit_text)

        appDataBase =
            Room.databaseBuilder(applicationContext, AppDataBase::class.java, "database_name")
                .build()

        taskChildDao = appDataBase.toDoChildDao
        taskParentDAO = appDataBase.toDoParentDao

        saveButton.setOnClickListener{
            saveToDatabase()
        }

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = toDoAdapter
        subscribeForToDoItems()

    }

    private fun subscribeForToDoItems() {
        lifecycleScope.launch {
            appDataBase.toDoChildDao.observeAllItems()
                .flowWithLifecycle(
                    lifecycle,
                    Lifecycle.State.STARTED
                )
                .distinctUntilChanged()
                .collectLatest { toDoListEntities ->
                    Log.d(TAG, "subscribeForToDoItems: ")

                    val toDoList: MutableList<ToDoBaseItem.ToDoItem> =
                        toDoListEntities.map {
                            ToDoBaseItem.ToDoItem(it.todoId ?: 0, it.toDoText, it.isChecked)
                        }.toMutableList()
                    val count = toDoList.count { it.isChecked }

                    val toDoListSorted: MutableList<ToDoBaseItem> =
                        toDoList.sortedBy { it.isChecked }.toMutableList()

                    //indexOfFirst - Returns index of the first element matching the given predicate, or -1 if the list does not contain such element.
                    val firstCheckedItemPosition =
                        toDoListSorted.indexOfFirst { (it as? ToDoBaseItem.ToDoItem)?.isChecked == true }

                    if (firstCheckedItemPosition == -1) {
                        // Don't need to add header if there is no any checked item
                    } else {
                        toDoListSorted.add(
                            firstCheckedItemPosition,
                            ToDoBaseItem.CheckedItemsHeader("Checked items $count")
                        )
                    }

                    toDoListSorted.add(
                        ToDoBaseItem.AddToDoItemButton
                    )

                    myList = toDoListSorted
                    toDoAdapter.submitList(toDoListSorted)
                }
        }
    }

    private fun addItem() {
        val updatedList = myList.toMutableList()
        val lastUncheckedItemIndex =
            updatedList.indexOfLast { it is ToDoBaseItem.ToDoItem && it.isChecked.not() } + 1
        updatedList.add(
            lastUncheckedItemIndex,
            ToDoBaseItem.ToDoItem(System.currentTimeMillis().toInt())
        )
        myList = updatedList
        toDoAdapter.submitList(updatedList)
    }

    private fun deleteItem(id: Int) {
        val updatedList = myList.toMutableList()
        updatedList.removeAll { it is ToDoBaseItem.ToDoItem && it.id == id }
        myList = updatedList
        toDoAdapter.submitList(updatedList)
    }

    private fun updateCheckState(id: Int, isChecked: Boolean) {
        Log.d(TAG, "updateCheckState: $myList")
        val toDoList: MutableList<ToDoBaseItem> = myList
            //map - можна обновляти дані в списку, а не тільки приводити до типу
            //copy - копіює всі дані, крім isChecked - його замінює на той, який передаю
            //else - якщо елемент не ToDoListItem або ToDoListItem, але не та id, то дані залишаємо як є
            .map { if(it is ToDoBaseItem.ToDoItem && it.id == id) it.copy(isChecked = isChecked) else it }
//            .filterIsInstance(ToDoBaseListItem.ToDoListItem::class.java)
            .toMutableList()
        val count = toDoList.count { (it as? ToDoBaseItem.ToDoItem)?.isChecked == true }

        val toDoListSorted: MutableList<ToDoBaseItem> =
            toDoList.sortedBy { (it is ToDoBaseItem.ToDoItem) && it.isChecked }.toMutableList()

        //indexOfFirst - Returns index of the first element matching the given predicate, or -1 if the list does not contain such element.
        val firstCheckedItemPosition =
            toDoListSorted.indexOfFirst { (it as? ToDoBaseItem.ToDoItem)?.isChecked == true }
// Якшо немає чекнутих айтемів , але є хедер для чекнутих, то прибрати хедер
        //то прибрати хедер який знаходиться під кнопкою add item
        val checkedItemsHeader = ToDoBaseItem.CheckedItemsHeader("Checked items $count")
        if (firstCheckedItemPosition == -1) {
            // Don't need to add header if there is no any checked item
            if(toDoListSorted.any { it is ToDoBaseItem.CheckedItemsHeader }){
                toDoListSorted.removeAll { it is ToDoBaseItem.CheckedItemsHeader }
            }
        } else {
            //any - вертає true,якщо хоча б один елемент відповідає умові
            if(toDoListSorted.any { it is ToDoBaseItem.CheckedItemsHeader }){ //якщо в відсортованому списку є чекед хедер
                val headerPosition = toDoListSorted.indexOfFirst { it is ToDoBaseItem.CheckedItemsHeader } //позиція хедера
                //set - замінюємо значення
                toDoListSorted.set(headerPosition, checkedItemsHeader)
            }else {
                toDoListSorted.add(
                    firstCheckedItemPosition,
                    checkedItemsHeader
                )
            }
        }

        val pressedItem = toDoListSorted.find { item -> (item as? ToDoBaseItem.ToDoItem)?.id == id }
        val addButtonIndex: Int = toDoListSorted.indexOfFirst { it is ToDoBaseItem.AddToDoItemButton }
        if(!isChecked){
            toDoListSorted.remove(pressedItem)
            //let - гарантує, те, що в фігурних дужках визветься тільки, якщо pressedItem не null
            //find може повернути null
            pressedItem?.let { toDoListSorted.add(addButtonIndex, pressedItem) }
        }

        Log.d(TAG, "updateCheckState: $isChecked")

        //myList = toDoListSorted
        toDoAdapter.submitList(toDoListSorted)

        myList = toDoListSorted
    }

    private fun updateText(id: Int, text: String){
        Log.d(TAG, "updateText: $id $text")
        myList = myList
            .map { if (it is ToDoBaseItem.ToDoItem && it.id == id) {
                it.copy(text = text)}
            else it }.toMutableList()
        Log.d(TAG, "updateText: $myList")
    }

    private fun saveToDatabase(){
        lifecycleScope.launch {
            val insertedParentId = taskParentDAO.insert(ToDoParentEntity(null, title = titleEditText.toString())).toInt()
            val toDoChildEntityList: MutableList<ToDoChildEntity> =
                myList.filterIsInstance<ToDoBaseItem.ToDoItem>().map{
                    ToDoChildEntity(todoId = null, toDoText = it.text.toString(), isChecked = it.isChecked, insertedParentId)
                }.toMutableList()
            toDoChildEntityList.forEach {
                taskChildDao.insert(it)
            }
        }
    }
}