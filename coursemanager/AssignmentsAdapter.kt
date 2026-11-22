package com.example.coursemanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AssignmentsAdapter(
    private val assignmentsList: List<Assignment>,
    private val onItemClick: (Assignment) -> Unit // Pass a click listener
) : RecyclerView.Adapter<AssignmentsAdapter.AssignmentsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.assignment_item, parent, false)
        return AssignmentsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssignmentsViewHolder, position: Int) {
        val assignment = assignmentsList[position]
        // Bind the data to the views in assignment_item.xml
        holder.assignmentTitleText.text = assignment.title
        holder.courseNameText.text = assignment.courseName
        holder.dueDateText.text = assignment.dueDate
        holder.descriptionText.text = assignment.description

        // Set a click listener on the item view
        holder.itemView.setOnClickListener {
            onItemClick(assignment) // Trigger the click listener with the assignment object
        }
    }

    override fun getItemCount(): Int = assignmentsList.size

    inner class AssignmentsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val assignmentTitleText: TextView = view.findViewById(R.id.assignmentTitleText)
        val courseNameText: TextView = view.findViewById(R.id.courseNameText)
        val dueDateText: TextView = view.findViewById(R.id.dueDateText)
        val descriptionText: TextView = view.findViewById(R.id.descriptionText)
    }
}



