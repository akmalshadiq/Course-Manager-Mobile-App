package com.example.coursemanager

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

class AssignmentActivity : AppCompatActivity() {

    private var isEditMode = false
    private var assignmentId: String? = null // Firebase key for the assignment
    private val db = FirebaseFirestore.getInstance()

    private lateinit var courseList: MutableList<String>
    private lateinit var courseAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_assignment)

        // Initialize course list and spinner
        courseList = mutableListOf()
        courseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, courseList)
        val courseSpinner: Spinner = findViewById(R.id.courseNameSpinner) // Ensure you have a Spinner in your layout
        courseSpinner.adapter = courseAdapter

        // Fetch courses from Firestore
        fetchCourses()

        // Get references to input fields
        val titleEditText = findViewById<EditText>(R.id.assignmentTitle)
        val dueDateEditText = findViewById<EditText>(R.id.assignmentDueDate)
        val descriptionEditText = findViewById<EditText>(R.id.assignmentDescription)
        val saveButton = findViewById<Button>(R.id.saveAssignmentButton)
        val deleteButton = findViewById<Button>(R.id.deleteAssignmentButton)

        // Check if this is an edit operation
        isEditMode = intent.hasExtra("assignmentId")
        if (isEditMode) {
            assignmentId = intent.getStringExtra("assignmentId")

            // Load existing assignment data
            loadAssignmentData(assignmentId, titleEditText, dueDateEditText, descriptionEditText)

            // Set button text for edit mode
            saveButton.text = "Update Assignment"
            deleteButton.isEnabled = true
        } else {
            // Add mode setup
            saveButton.text = "Add Assignment"
            deleteButton.isEnabled = false
        }

        // Save button click listener
        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val courseName = courseSpinner.selectedItem.toString() // Get the selected course from the Spinner
            val dueDate = dueDateEditText.text.toString()
            val description = descriptionEditText.text.toString()

            // Validate inputs
            if (title.isNotEmpty() && courseName.isNotEmpty() && dueDate.isNotEmpty() && description.isNotEmpty()) {
                saveOrUpdateAssignment(title, courseName, dueDate, description)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Delete button click listener
        deleteButton.setOnClickListener {
            deleteAssignment(assignmentId)
        }
    }

    private fun fetchCourses() {
        db.collection("Course")
            .get()
            .addOnSuccessListener { querySnapshot ->
                courseList.clear()  // Clear previous courses
                for (doc in querySnapshot) {
                    val courseName = doc.getString("courseName") ?: ""
                    courseList.add(courseName)
                }
                courseAdapter.notifyDataSetChanged()  // Update the spinner with the new course list
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch courses: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Load existing assignment data
    private fun loadAssignmentData(
        assignmentId: String?,
        titleEditText: EditText,
        dueDateEditText: EditText,
        descriptionEditText: EditText
    ) {
        if (assignmentId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid Assignment ID", Toast.LENGTH_SHORT).show()
            return
        }

        val email = UserSession.currentStudentEmail ?: ""
        val assignmentRef = db.collection("Assignment").document(email)

        assignmentRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val assignmentsMap = document.get("assignments") as? Map<String, Map<String, Any>>
                val assignmentData = assignmentsMap?.get(assignmentId)

                if (assignmentData != null) {
                    titleEditText.setText(assignmentData["title"] as? String ?: "")
                    dueDateEditText.setText(assignmentData["dueDate"] as? String ?: "")
                    descriptionEditText.setText(assignmentData["description"] as? String ?: "")
                    val courseName = assignmentData["courseName"] as? String ?: ""
                    // Set the course name into the Spinner (if it exists)
                    val spinnerPosition = courseAdapter.getPosition(courseName)
                    findViewById<Spinner>(R.id.courseNameSpinner).setSelection(spinnerPosition)
                } else {
                    Toast.makeText(this, "Assignment not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Assignment document not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to load assignment: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Save or Update assignment
    private fun saveOrUpdateAssignment(
        title: String,
        courseName: String,
        dueDate: String,
        description: String
    ) {
        val email = UserSession.currentStudentEmail ?: ""
        val assignmentsRef = db.collection("Assignment").document(email)

        if (isEditMode) {
            val localAssignmentId = assignmentId // Store in a local variable for smart casting

            if (localAssignmentId.isNullOrEmpty()) {
                Toast.makeText(this, "Invalid Assignment ID", Toast.LENGTH_SHORT).show()
                return
            }

            val assignment = mapOf(
                "id" to localAssignmentId,
                "title" to title,
                "courseName" to courseName,
                "dueDate" to dueDate,
                "description" to description
            )

            // Update the assignments map
            assignmentsRef.update("assignments.$localAssignmentId", assignment)
                .addOnSuccessListener {
                    Toast.makeText(this, "Assignment updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update assignment: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Add new assignment
            val newAssignmentId = assignmentsRef.collection("temp").document().id // Generate a unique ID
            val assignment = mapOf(
                "id" to newAssignmentId,
                "title" to title,
                "courseName" to courseName,
                "dueDate" to dueDate,
                "description" to description
            )

            assignmentsRef.update("assignments.$newAssignmentId", assignment)
                .addOnSuccessListener {
                    Toast.makeText(this, "Assignment added successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                        // Document does not exist, create a new one
                        assignmentsRef.set(mapOf("assignments" to mapOf(newAssignmentId to assignment)))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Assignment added successfully!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { ex ->
                                Toast.makeText(this, "Failed to add assignment: ${ex.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Failed to add assignment: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // Delete Assignment
    private fun deleteAssignment(assignmentId: String?) {
        if (assignmentId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid Assignment ID", Toast.LENGTH_SHORT).show()
            return
        }

        val email = UserSession.currentStudentEmail ?: ""
        val assignmentsRef = db.collection("Assignment").document(email)

        // Remove the assignment from the assignments map
        assignmentsRef.update("assignments.$assignmentId", FieldValue.delete())
            .addOnSuccessListener {
                Toast.makeText(this, "Assignment deleted successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete assignment: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}