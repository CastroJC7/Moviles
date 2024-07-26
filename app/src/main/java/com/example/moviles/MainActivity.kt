package com.example.moviles


import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.app.AlertDialog

class MainActivity : AppCompatActivity() {
    private lateinit var editTextTask: EditText
    private lateinit var buttonAddTask: Button
    private lateinit var listViewTasks: ListView
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextTask = findViewById(R.id.editTextTask)
        buttonAddTask = findViewById(R.id.buttonAddTask)
        listViewTasks = findViewById(R.id.listViewTasks)

        taskAdapter = TaskAdapter(this, tasks)
        listViewTasks.adapter = taskAdapter

        loadTasks()

        buttonAddTask.setOnClickListener {
            val taskName = editTextTask.text.toString().trim()
            if (taskName.isNotEmpty()) {
                val task = Task(taskName, false)
                tasks.add(task)
                taskAdapter.notifyDataSetChanged()
                editTextTask.text.clear()
                saveTasks()
            }
        }
    }

    private fun loadTasks() {
        val sharedPreferences = getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)
        val taskSet = sharedPreferences.getStringSet("tasks", HashSet<String>()) ?: HashSet()
        tasks.clear()
        for (taskString in taskSet) {
            val parts = taskString.split("|")
            if (parts.size == 2) {
                tasks.add(Task(parts[0], parts[1].toBoolean()))
            }
        }
        taskAdapter.notifyDataSetChanged()
    }

    private fun saveTasks() {
        val sharedPreferences = getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val taskSet = HashSet<String>()
        for (task in tasks) {
            taskSet.add("${task.name}|${task.isCompleted}")
        }
        editor.putStringSet("tasks", taskSet)
        editor.apply()
    }

    private fun showEditDialog(position: Int) {
        val task = tasks[position]
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.edit_task_dialog, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextTaskName)

        editText.setText(task.name)

        builder.setView(dialogLayout)
        builder.setTitle("Editar Tarea")
        builder.setPositiveButton("Guardar") { _, _ ->
            val editedTaskName = editText.text.toString()
            if (editedTaskName.isNotEmpty()) {
                tasks[position] = Task(editedTaskName, task.isCompleted)
                taskAdapter.notifyDataSetChanged()
                saveTasks()
            }
        }
        builder.setNegativeButton("Cancelar", null)

        val dialog = builder.create()
        dialog.show()
    }

    inner class TaskAdapter(context: Context, private val tasks: MutableList<Task>) : BaseAdapter() {
        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun getCount(): Int = tasks.size
        override fun getItem(position: Int): Any = tasks[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View
            val holder: ViewHolder

            if (convertView == null) {
                view = inflater.inflate(R.layout.task_item, parent, false)
                holder = ViewHolder()
                holder.checkBox = view.findViewById(R.id.checkBoxTask)
                holder.textView = view.findViewById(R.id.textViewTask)
                holder.editButton = view.findViewById(R.id.buttonEditTask)
                holder.deleteButton = view.findViewById(R.id.buttonDeleteTask)
                view.tag = holder
            } else {
                view = convertView
                holder = convertView.tag as ViewHolder
            }

            val task = tasks[position]
            holder.checkBox.isChecked = task.isCompleted
            holder.textView.text = task.name
            if (task.isCompleted) {
                holder.textView.paintFlags = holder.textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                holder.textView.paintFlags = holder.textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                task.isCompleted = isChecked
                notifyDataSetChanged()
                saveTasks()
            }

            holder.editButton.setOnClickListener {
                showEditDialog(position)
            }

            holder.deleteButton.setOnClickListener {
                if (position < tasks.size) {
                    tasks.removeAt(position)
                    notifyDataSetChanged()
                    saveTasks()
                }
            }
            return view
        }
    }

    private inner class ViewHolder {
        lateinit var checkBox: CheckBox
        lateinit var textView: TextView
        lateinit var editButton: Button
        lateinit var deleteButton: Button
    }

    data class Task(val name: String, var isCompleted: Boolean)
}
