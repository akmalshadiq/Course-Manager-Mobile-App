package com.example.coursemanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ListCoursesActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private val courseList = mutableListOf<Course>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: CourseAdapter

    // Define a launcher for AddCourseActivity
    private val addCourseLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                fetchCourses()  // Refresh the course list after adding a new course
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_courses)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewCourses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Setup adapter
        adapter = CourseAdapter(courseList) { selectedCourse ->
            // Handle course click, navigate to MainActivity with course data
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("COURSE_ID", selectedCourse.id)
                putExtra("COURSE_NAME", selectedCourse.courseName)
                putExtra("COURSE_CODE", selectedCourse.courseCode)
                putExtra("INSTRUCTOR", selectedCourse.instructor)
                putExtra("SCHEDULE", selectedCourse.schedule)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Fetch existing courses from Firestore
        fetchCourses()

        // Handle "Add New Course" button click
        val addNewCourseButton: Button = findViewById(R.id.addNewCourseButton)
        addNewCourseButton.setOnClickListener {
            // Navigate to AddCourseActivity and expect a result
            val intent = Intent(this, AddCourseActivity::class.java)
            addCourseLauncher.launch(intent)
        }
    }

    // Fetch courses from Firestore and update the RecyclerView
    private fun fetchCourses() {
        db.collection("Course")
            .get()
            .addOnSuccessListener { querySnapshot ->
                courseList.clear()  // Clear previous courses
                for (doc in querySnapshot) {
                    val course = doc.toObject(Course::class.java).copy(id = doc.id)
                    courseList.add(course)
                }
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch courses: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
