package com.example.todonotes

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalArgumentException

class ToDoAdapter(
    private val deleteItemCallback: (Int) -> Unit,
    private val itemCheckedCallback: (Int, Boolean) -> Unit,
    private val addItemCallback: () -> Unit,
    private val updateTextCallback: (Int, String) -> Unit
) :
    ListAdapter<ToDoBaseItem, RecyclerView.ViewHolder>(diffCallback) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ToDoBaseItem.CheckedItemsHeader -> CHECKED_HEADER_VIEW_TYPE
            is ToDoBaseItem.ToDoItem -> TODO_ITEM_VIEW_TYPE
            ToDoBaseItem.AddToDoItemButton -> ADD_TO_DO_VIEW_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            CHECKED_HEADER_VIEW_TYPE -> {
                val viewHeader = LayoutInflater.from(parent.context)
                    .inflate(R.layout.checked_header_list_item, parent, false)
                return CheckedItemsViewHolder(viewHeader)
            }

            TODO_ITEM_VIEW_TYPE -> {
                val viewToDoItems = LayoutInflater.from(parent.context)
                    .inflate(R.layout.to_do_item, parent, false)
                return ToDoViewHolder(viewToDoItems)
            }

            ADD_TO_DO_VIEW_TYPE -> {
                val viewAddToDoItem = LayoutInflater.from(parent.context)
                    .inflate(R.layout.add_to_do_item, parent, false)
                return AddToDoItemViewHolder(viewAddToDoItem)
            }

            else -> throw IllegalArgumentException("No such view type - $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CheckedItemsViewHolder) {
            holder.headerCheckedTextView.text =
                (getItem(position) as ToDoBaseItem.CheckedItemsHeader).text
        } else if (holder is AddToDoItemViewHolder) {
            holder.addToDoItemButton.setOnClickListener { addItemCallback() }
        } else if (holder is ToDoViewHolder) {
            val toDoItem = getItem(position) as ToDoBaseItem.ToDoItem
            holder.checkBox.isChecked = toDoItem.isChecked
            holder.removeTextChangeListener()
            holder.toDoItemText.setText(toDoItem.text)

            holder.addTextChangeListener(toDoItem.id)

            holder.deleteItem.setOnClickListener(View.OnClickListener {
                deleteItemCallback(toDoItem.id)
            })
            holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                //isPressed - коли юзер фізично натисне на чекбокс
                if (buttonView.isPressed.not()) return@setOnCheckedChangeListener
                itemCheckedCallback(toDoItem.id, isChecked)
            }
        }
    }

    inner class ToDoViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val toDoItemText: EditText = itemView.findViewById(R.id.toDoItem_edit_text)
        val deleteItem: AppCompatImageButton = itemView.findViewById(R.id.delete_button)
        val textChangeListener = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                id?.let { updateTextCallback(it, s.toString()) }
            }
        }

        var id: Int? = null

        fun removeTextChangeListener(){
            toDoItemText.removeTextChangedListener(textChangeListener)
        }
        fun addTextChangeListener(id: Int){
            toDoItemText.addTextChangedListener(textChangeListener)
            this.id = id
        }
    }

    class CheckedItemsViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerCheckedTextView: TextView = itemView.findViewById(R.id.checked_header_text_view)
    }

    class AddToDoItemViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addToDoItemButton: Button = itemView.findViewById(R.id.add_item_button)
    }

    companion object {
        private const val TODO_ITEM_VIEW_TYPE = 1
        private const val CHECKED_HEADER_VIEW_TYPE = 2
        private const val ADD_TO_DO_VIEW_TYPE = 3

        private val diffCallback = object : DiffUtil.ItemCallback<ToDoBaseItem>() {
            override fun areItemsTheSame(
                oldItem: ToDoBaseItem,
                newItem: ToDoBaseItem
            ): Boolean {
                return when (oldItem) {
                    is ToDoBaseItem.CheckedItemsHeader -> oldItem.text == (newItem as? ToDoBaseItem.CheckedItemsHeader)?.text
                    ToDoBaseItem.AddToDoItemButton -> oldItem == newItem
                    is ToDoBaseItem.ToDoItem -> oldItem.id == (newItem as? ToDoBaseItem.ToDoItem)?.id
                }
            }

            override fun areContentsTheSame(
                oldItem: ToDoBaseItem,
                newItem: ToDoBaseItem
            ): Boolean {
                return when (oldItem) {
                    is ToDoBaseItem.CheckedItemsHeader -> false
                    is ToDoBaseItem.ToDoItem -> oldItem == (newItem as? ToDoBaseItem.ToDoItem)
                    ToDoBaseItem.AddToDoItemButton -> false
                }
            }
        }
    }
}