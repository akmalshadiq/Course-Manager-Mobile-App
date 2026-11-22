package com.example.coursemanager

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditCourseActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var courseId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_course)

        // Initialize the EditText and Button views
        val editCourseName: EditText = findViewById(R.id.editCourseName)
        val editCourseCode: EditText = findViewById(R.id.editCourseCode)
        val editInstructor: EditText = findViewById(R.id.editInstructor)
        val editSchedule: EditText = findViewById(R.id.editSchedule)
        val btnSave: Button = findViewById(R.id.saveButton)
        val tvHeaderTitle: TextView = findViewById(R.id.courseDetailsTitle)
        val backButton: ImageView = findViewById(R.id.back_button)

        // Set header title
        tvHeaderTitle.text = "Edit Course"

        // Retrieve course data from the intent
        courseId = intent.getStringExtra("COURSE_ID") ?: ""

        // Set the form fields from the passed-in Intent extras
        editCourseName.setText(intent.getStringExtra("COURSE_NAME"))
        editCourseCode.setText(intent.getStringExtra("COURSE_CODE"))
        editInstructor.setText(intent.getStringExtra("INSTRUCTOR"))
        editSchedule.setText(intent.getStringExtra("SCHEDULE"))

        // Handle Save button click
        btnSave.setOnClickListener {
            val courseName = editCourseName.text.toString()
            val courseCode = editCourseCode.text.toString()
            val instructor = editInstructor.text.toString()
            val schedule = editSchedule.text.toString()

            if (courseName.isEmpty() || courseCode.isEmpty() || instructor.isEmpty() || schedule.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update course data in Firestore
            db.collection("Course").document(courseId) // Use the actual document ID from Firestore
                .update(
                    "courseName", courseName,
                    "courseCode", courseCode,
                    "Instructor", instructor,
                    "Schedule", schedule
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "Course updated successfully", Toast.LENGTH_SHORT).show()
                    finish() // Return to the previous screen
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Handle back button click
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()  // Handles going back to the previous activity
        }
    }
}
