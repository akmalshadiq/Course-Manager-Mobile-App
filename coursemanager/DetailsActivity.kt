package com.example.coursemanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var universityEditText: EditText
    private lateinit var facultyEditText: EditText
    private lateinit var logoutButton: Button
    private lateinit var editButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize the UI components
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        universityEditText = findViewById(R.id.universityEditText)
        facultyEditText = findViewById(R.id.facultyEditText)
        logoutButton = findViewById(R.id.logoutButton)
        editButton = findViewById(R.id.editButton)

        // Fetch user details
        fetchUserDetails()

        // Set up Logout button click listener
        logoutButton.setOnClickListener {
            signOutUser()
        }

        // Set up Edit button click listener
        editButton.setOnClickListener {
            saveUserDetails()
        }
    }

    // Function to fetch user details from Firestore based on email
    private fun fetchUserDetails() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userEmail = user.email
            if (userEmail != null) {
                // Query Firestore to find the document where "Email" matches the logged-in user's email
                db.collection("Details")
                    .whereEqualTo("Email", userEmail)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val document = querySnapshot.documents[0]
                            // Populate the EditText fields with the fetched data
                            val name = document.getString("Name") ?: "No Name"
                            val email = document.getString("Email") ?: "No Email"
                            val university = document.getString("University") ?: "No University"
                            val faculty = document.getString("Faculty") ?: "No Faculty"

                            nameEditText.setText(name)
                            emailEditText.setText(email)
                            universityEditText.setText(university)
                            facultyEditText.setText(faculty)
                        } else {
                            Toast.makeText(this, "No user data found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to fetch data: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to save user details to Firestore
    private fun saveUserDetails() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userEmail = user.email
            if (userEmail != null) {
                // Get the data from EditText fields
                val name = nameEditText.text.toString().trim()
                val email = emailEditText.text.toString().trim()
                val university = universityEditText.text.toString().trim()
                val faculty = facultyEditText.text.toString().trim()

                // Create a hash map to represent the user's details
                val userDetails = hashMapOf(
                    "Name" to name,
                    "Email" to email,
                    "University" to university,
                    "Faculty" to faculty
                )

                // Find matching email and update it or add new data
                db.collection("Details")
                    .whereEqualTo("Email", userEmail)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            // If document exists, update it
                            val documentId = querySnapshot.documents[0].id
                            db.collection("Details").document(documentId)
                                .set(userDetails)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Details updated successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this, "Failed to update details: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // If document does not exist, add new data
                            db.collection("Details")
                                .add(userDetails)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Details saved successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this, "Failed to save details: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to find user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to sign out user and navigate to LoginActivity
    private fun signOutUser() {
        FirebaseAuth.getInstance().signOut() // Sign out the user
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)

        // Finish the current activity
        finish()
    }
}
