package com.example.coursemanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddCourseActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_course)

        val editCourseName = findViewById<EditText>(R.id.editNewCourseName)
        val editCourseCode = findViewById<EditText>(R.id.editNewCourseCode)
        val editInstructor = findViewById<EditText>(R.id.editNewInstructor)
        val editSchedule = findViewById<EditText>(R.id.editNewSchedule)
        val btnAddCourse = findViewById<Button>(R.id.btnAddCourse)
        val backButton = findViewById<ImageView>(R.id.back_button)

        // Set click listener on back button
        backButton.setOnClickListener {
            // Navigate back to the previous activity using the OnBackPressedDispatcher
            onBackPressedDispatcher.onBackPressed()
        }


        btnAddCourse.setOnClickListener {
            val courseName = editCourseName.text.toString()
            val courseCode = editCourseCode.text.toString()
            val instructor = editInstructor.text.toString()
            val schedule = editSchedule.text.toString()

            if (courseName.isNotBlank() && courseCode.isNotBlank() && instructor.isNotBlank() && schedule.isNotBlank()) {
                addCourseToFirestore(courseName, courseCode, instructor, schedule)
            } else {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addCourseToFirestore(courseName: String, courseCode: String, instructor: String, schedule: String) {
        val newCourse = Course(courseName, courseCode, instructor, schedule)

        db.collection("Course")
            .add(newCourse)
            .addOnSuccessListener { documentRef ->
                newCourse.id = documentRef.id

                // Return the result to ListCoursesActivity
                val resultIntent = Intent()
                setResult(RESULT_OK, resultIntent)  // Signal that the course was added successfully
                finish()  // Close AddCourseActivity and return to ListCoursesActivity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add course: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

