package com.example.coursemanager

data class Task(
    val id: String = "",
    val taskName: String = "",
    val courseAssociation: String = "",
    val dueDate: String = "",
    val label: String = ""
)
