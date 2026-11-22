package com.example.coursemanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class DashboardActivity : AppCompatActivity() {

    private lateinit var assignmentsRecyclerView: RecyclerView
    private lateinit var assignmentsAdapter: AssignmentsAdapter
    private val allAssignments = mutableListOf<Assignment>()
    private lateinit var db: FirebaseFirestore
    private var listenerRegistration: ListenerRegistration? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()

        // Initialize RecyclerView
        assignmentsRecyclerView = findViewById(R.id.allAssignmentsRecyclerView)
        assignmentsRecyclerView.layoutManager = LinearLayoutManager(this)
        assignmentsAdapter = AssignmentsAdapter(allAssignments) { assignment ->
            // Navigate to AssignmentActivity
            val intent = Intent(this, AssignmentActivity::class.java)
            intent.putExtra("assignmentId", assignment.id) // Pass the assignment ID
            startActivity(intent)
        }
        assignmentsRecyclerView.adapter = assignmentsAdapter

        // Start listening to Firestore changes
        startListeningToAssignments()

        // Buttons
        val addAssignmentButton = findViewById<Button>(R.id.addAssignmentButton)
        addAssignmentButton.text = "Add Assignment"

        val profileButton = findViewById<Button>(R.id.profileButton)
        val notificationButton = findViewById<Button>(R.id.notificationButton)
        val coursesButton = findViewById<Button>(R.id.coursesButton)
        val taskButton = findViewById<Button>(R.id.taskButton)

        // Navigate to Add Assignment
        addAssignmentButton.setOnClickListener {
            val intent = Intent(this, AssignmentActivity::class.java)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, DetailsActivity::class.java)
            startActivity(intent)
        }

        notificationButton.setOnClickListener {
            Toast.makeText(this, "Notification clicked", Toast.LENGTH_SHORT).show()
        }

        taskButton.setOnClickListener {
            val intent = Intent(this, Reminder::class.java)
            startActivity(intent)
        }

        // Navigate to MainActivity
        coursesButton.setOnClickListener {
            val intent = Intent(this, ListCoursesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startListeningToAssignments() {
        val email = UserSession.currentStudentEmail ?: ""

        listenerRegistration = db.collection("Assignment")
            .document(email)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Log.e("DashboardActivity", "Error listening for changes", error)
                    Toast.makeText(this, "Failed to listen for changes", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val assignmentsMap = documentSnapshot.get("assignments") as? Map<String, Map<String, Any>>
                    allAssignments.clear()

                    assignmentsMap?.forEach { (id, assignmentData) ->
                        val assignment = Assignment(
                            id = id,
                            title = assignmentData["title"] as? String ?: "",
                            courseName = assignmentData["courseName"] as? String ?: "",
                            dueDate = assignmentData["dueDate"] as? String ?: "",
                            description = assignmentData["description"] as? String ?: "",
                            email = email
                        )
                        allAssignments.add(assignment)
                    }

                    // Notify adapter of data changes
                    assignmentsAdapter.notifyDataSetChanged()
                } else {
                    // No assignments found
                    allAssignments.clear()
                    assignmentsAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "No assignments found.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop listening to Firestore changes
        listenerRegistration?.remove()
    }
}
