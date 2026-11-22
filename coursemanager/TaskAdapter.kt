package com.example.coursemanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TasksAdapter(
    private val tasks: List<Task>,
    private val onTaskClick: (Task) -> Unit
) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false) // Replace with your item layout
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = tasks.size

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskName: TextView = itemView.findViewById(R.id.taskName)
        private val courseAssociation: TextView = itemView.findViewById(R.id.courseAssociation)
        private val taskDueDate: TextView = itemView.findViewById(R.id.taskDueDate)
        private val taskLabel: TextView = itemView.findViewById(R.id.taskLabel)

        fun bind(task: Task) {
            taskName.text = task.taskName
            courseAssociation.text = task.courseAssociation
            taskDueDate.text = task.dueDate
            taskLabel.text = task.label

            itemView.setOnClickListener {
                onTaskClick(task)
            }
        }
    }
}
