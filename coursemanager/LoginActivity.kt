package com.example.coursemanager

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Find Views from the layout
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)
        val createAccountButton: Button = findViewById(R.id.createAccountButton)
        val forgotPasswordText: TextView = findViewById(R.id.forgotPasswordText)
        val passwordVisibilityToggle: ImageView = findViewById(R.id.eyeIcon)

        // Adjust padding for system bars (for edge-to-edge handling)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var isPasswordVisible = false

        // Toggle password visibility
        passwordVisibilityToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordVisibilityToggle.setImageResource(R.drawable.eyeopen)
            } else {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordVisibilityToggle.setImageResource(R.drawable.eyeclose)
            }
            // Retain cursor position
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        // Handle Login button click
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signIn(email, password)
        }

        // Handle Create Account button click
        createAccountButton.setOnClickListener {
            navigateToRegister()
        }

        // Handle Forgot Password click
        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success
                    Log.d("LoginActivity", "signInWithEmail:success")
                    val user = auth.currentUser

                    // Check if email is verified
                    if (user != null && user.isEmailVerified) {
                        Toast.makeText(this, "Welcome back, ${user.email}!", Toast.LENGTH_SHORT).show()
                        updateUI(user)
                    } else {
                        // Email is not verified, prompt user to check their inbox
                        Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show()
                        auth.signOut() // Optionally sign out the user after unsuccessful login attempt
                    }
                } else {
                    // Sign-in failure
                    Log.w("LoginActivity", "signInWithEmail:failure", task.exception)

                    // Handle Firebase authentication errors
                    val exception = task.exception
                    val errorMessage = when {
                        exception is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                            if (exception.message?.contains("The password is invalid") == true) {
                                "The password is invalid or the user does not have a password."
                            } else {
                                "The email address is badly formatted."
                            }
                        }
                        exception is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                            "There is no user record corresponding to this identifier."
                        }
                        else -> {
                            "Authentication failed: ${exception?.message}"
                        }
                    }

                    // Show the appropriate error message in a toast
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    updateUI(null) // Do not navigate
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Redirect to another activity after successful login
            UserSession.currentStudentEmail = user.email // Store the email globally
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("USER_EMAIL", user.email) // Pass user email to DetailsActivity
            startActivity(intent)
            finish() // Finish LoginActivity to prevent going back to it
        }
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        // Check if the user is already signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Optional: Provide a message or preload data but do not navigate
            Log.d("LoginActivity", "User already signed in: ${currentUser.email}")
        }
    }
}
