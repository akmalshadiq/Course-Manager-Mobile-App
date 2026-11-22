package com.example.coursemanager

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    // Declare FirebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registeractivity)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Find Views from the layout
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirmPasswordEditText)
        val registerButton: Button = findViewById(R.id.registerButton)
        val passwordVisibilityToggle: ImageView = findViewById(R.id.eyeIcon)

        var isPasswordVisible = false

        // Toggle password visibility for both password and confirm password fields
        passwordVisibilityToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                confirmPasswordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordVisibilityToggle.setImageResource(R.drawable.eyeopen)
            } else {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                confirmPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordVisibilityToggle.setImageResource(R.drawable.eyeclose)
            }
            // Retain cursor position in both fields
            passwordEditText.setSelection(passwordEditText.text.length)
            confirmPasswordEditText.setSelection(confirmPasswordEditText.text.length)
        }

        // Handle Register button click
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (validateInputs(email, password, confirmPassword)) {
                createAccount(email, password)
            }
        }
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Please fill out all fields")
            return false
        }
        if (password != confirmPassword) {
            showToast("Passwords do not match")
            return false
        }
        if (password.length < 6) {
            showToast("Password should be at least 6 characters")
            return false
        }
        // Password validation: Check for special characters
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*".toRegex())) {
            showToast("Password must contain at least one special character")
            return false
        }
        return true
    }

    private fun createAccount(email: String, password: String) {
        // Validate the password according to the policy
        if (!validateInputs(email, password, password)) return

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful
                    Log.d("RegisterActivity", "createUserWithEmail:success")

                    val user = auth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { emailVerificationTask ->
                            if (emailVerificationTask.isSuccessful) {
                                Log.d("RegisterActivity", "Email verification sent")
                                showToast("Registration Successful! A verification email has been sent. Please verify your email before logging in.")
                                updateUI()
                            } else {
                                Log.w("RegisterActivity", "Failed to send email verification", emailVerificationTask.exception)
                                showToast("Failed to send verification email. Please try again.")
                            }
                        }
                } else {
                    // Registration failed
                    val exceptionMessage = task.exception?.localizedMessage ?: "Unknown error"
                    Log.w("RegisterActivity", "createUserWithEmail:failure - $exceptionMessage")
                    showToast("Registration failed: $exceptionMessage")
                }
            }
    }


    //Update the UI after successful registration.

    private fun updateUI() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


      //Show a toast message.

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    public override fun onStart() {
        super.onStart()
        // Optionally sign out the current user to ensure new registration flow
        auth.currentUser?.let {
            auth.signOut()
        }
    }
}
