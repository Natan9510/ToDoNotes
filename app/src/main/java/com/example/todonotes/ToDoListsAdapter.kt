package com.example.todonotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalArgumentException

class ToDoListsAdapter (
    private val addListCallback: () -> Unit
) : ListAdapter<ToDoBaseListItem, RecyclerView.ViewHolder>(diffItemCallback) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)){
            is ToDoBaseListItem.ToDoListItem -> TODO_LIST_VIEW_TYPE
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            TODO_LIST_VIEW_TYPE -> {
                val viewToDoList = LayoutInflater.from(parent.context)
                    .inflate(R.layout.to_do_list_item, parent, false)
                return ToDoListViewHolder(viewToDoList)
            }

            else -> throw IllegalArgumentException("No such view type - $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val toDoListItem = getItem(position) as ToDoBaseListItem.ToDoListItem
        (holder as ToDoListViewHolder).title.setText(toDoListItem.title)
    }

    class ToDoListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val title: TextView = itemView.findViewById(R.id.title_to_do_list_textview)
    }

    companion object {
        private const val TODO_LIST_VIEW_TYPE = 1

        private val diffItemCallback = object : DiffUtil.ItemCallback<ToDoBaseListItem>(){
            override fun areItemsTheSame(
                oldItem: ToDoBaseListItem,
                newItem: ToDoBaseListItem
            ): Boolean {
                return when (oldItem){
                    is ToDoBaseListItem.ToDoListItem -> oldItem.title == (newItem as? ToDoBaseListItem.ToDoListItem)?.title
                }
            }

            override fun areContentsTheSame(
                oldItem: ToDoBaseListItem,
                newItem: ToDoBaseListItem
            ): Boolean {
                return when(oldItem){
                    is ToDoBaseListItem.ToDoListItem -> oldItem == (newItem as? ToDoBaseListItem.ToDoListItem)
                }
            }
        }
    }
}




