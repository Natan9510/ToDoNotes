package com.example.todonotes

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ToDoListsActivity : ComponentActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var appDataBase: AppDataBase
    lateinit var taskParentDAO: ToDoParentDAO
    lateinit var taskChildDAO: ToDoChildDAO
    lateinit var addToDoListButton: ImageButton

    val toDoListsAdapter: ToDoListsAdapter = ToDoListsAdapter(
        onToDoListClickedCallback = { toDoBaseListItem -> onToDoListClicked(toDoBaseListItem)},
        showAlertDialogCallback = {parentId ->  showAlertDialog(parentId)})


    var listOfLists: MutableList<ToDoBaseListItem> = mutableListOf()

    val TAG: String = "zlo"

    val startForResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data= result.data
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.to_do_lists_activity)

        Log.d(TAG, "onCreate: todolistsactivity")

        appDataBase = Room.databaseBuilder(applicationContext, AppDataBase::class.java, "database_name")
            .build()
        taskParentDAO = appDataBase.toDoParentDao
        taskChildDAO = appDataBase.toDoChildDao

        recyclerView = findViewById(R.id.recycler_view_for_lists)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = toDoListsAdapter

        addToDoListButton = findViewById(R.id.add_to_do_list_button)
        addToDoListButton.setOnClickListener {
            addToDoList()
        }

//        listOfLists.add(ToDoBaseListItem.ToDoListItem(null, "Hello"))
//        toDoListsAdapter.submitList(listOfLists)
        lifecycleScope.launch {
            appDataBase.toDoParentDao.observeAllItems()
                .flowWithLifecycle(
                    lifecycle,
                    Lifecycle.State.STARTED
                )
                .collectLatest { toDoListEntities ->
                    val toDoLists: MutableList<ToDoBaseListItem> =
                        toDoListEntities.map {
                            ToDoBaseListItem.ToDoListItem(it.parentId, it.title)
                        }.toMutableList()
                    toDoListsAdapter.submitList(toDoLists)
                }
        }

    }

    private fun addToDoList(){
//        val updatedList = listOfLists
//        updatedList.add(ToDoBaseListItem.ToDoListItem(System.currentTimeMillis().toInt()))
//        listOfLists = updatedList
//        toDoListsAdapter.submitList(updatedList)
        val intent: Intent = Intent(applicationContext, MainActivity::class.java)
        startForResult.launch(intent)
    }

    private fun onToDoListClicked(toDoBaseListItem: ToDoBaseListItem) {
        val intent: Intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("parent_id", (toDoBaseListItem as ToDoBaseListItem.ToDoListItem).id)
        startForResult.launch(intent)
    }

    private fun showAlertDialog(parentId: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this) // context?
        builder
            .setMessage("Do you want to delete this list?")
            .setPositiveButton("Yes"){ dialog, which ->
                deleteToDoList(parentId)
                dialog.dismiss()
            }
            .setNegativeButton("No"){ dialog, which ->
                dialog.dismiss()
            }.create().show()
    }

    private fun deleteToDoList(parentId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            appDataBase.toDoChildDao.deleteByLongClickOnList(parentId)
            appDataBase.toDoParentDao.delete(parentId)
        }
    }
}