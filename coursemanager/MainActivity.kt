package com.example.coursemanager

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private var courseId: String? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Set the content view to the XML layout
        setContentView(R.layout.activity_main)

        // Get the course ID from the Intent (if any)
        courseId = intent.getStringExtra("COURSE_ID")

        // Fetch data from Firestore and display it in the layout
        fetchFirestoreData(courseId)

        // Set up the Edit button click listener
        val editButton: ImageView = findViewById(R.id.edit_icon)
        editButton.setOnClickListener {
            // Create an Intent to navigate to EditCourseActivity
            val intent = Intent(this, EditCourseActivity::class.java)

            // Pass the current course ID to the EditCourseActivity
            courseId?.let {
                intent.putExtra("COURSE_ID", it)
            }

            // Start EditCourseActivity
            startActivity(intent)
        }

        // Set up the Delete button click listener
        val deleteButton: ImageView = findViewById(R.id.delete_icon)
        deleteButton.setOnClickListener {
            // Call delete function when delete button is clicked
            deleteCourse(courseId)
        }

        // Set up the Back button click listener
        val backButton: ImageView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            // Navigate back to ListCoursesActivity
            val intent = Intent(this, ListCoursesActivity::class.java)
            startActivity(intent)
            finish() // Close the current activity
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-fetch Firestore data whenever MainActivity is resumed
        fetchFirestoreData(courseId)
    }

    private fun fetchFirestoreData(courseId: String?) {
        // If courseId is null or empty, do nothing
        if (courseId.isNullOrEmpty()) {
            return
        }

        val documentRef = db.collection("Course").document(courseId)

        // Fetch Firestore data
        documentRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Extract the fields
                    val courseName = document.getString("courseName") ?: "N/A"
                    val courseCode = document.getString("courseCode") ?: "N/A"
                    val instructor = document.getString("instructor") ?: "N/A"
                    val schedule = document.getString("schedule") ?: "N/A"

                    // Update the TextViews with the fetched data
                    findViewById<TextView>(R.id.course_name).text = "Course Name: $courseName"
                    findViewById<TextView>(R.id.course_code).text = "Course Code: $courseCode"
                    findViewById<TextView>(R.id.course_instructor).text = "Instructor: $instructor"
                    findViewById<TextView>(R.id.course_schedule).text = "Schedule: $schedule"
                }
            }
            .addOnFailureListener {
                // Handle any errors if needed
                Toast.makeText(this, "Failed to fetch course data.", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to delete the course from Firestore
    private fun deleteCourse(courseId: String?) {
        if (courseId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid course ID", Toast.LENGTH_SHORT).show()
            return
        }

        // Deleting the course from Firestore
        db.collection("Course").document(courseId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Course deleted successfully", Toast.LENGTH_SHORT).show()
                finish() // Close the current activity and return to the previous screen
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete course: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
