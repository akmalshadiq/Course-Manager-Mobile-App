package com.example.coursemanager

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity


class WelcomePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcomepageactivity) // Ensure you have a layout file for the welcome page

        // Delay for 3 seconds and then navigate to LoginActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close WelcomePageActivity so the user cannot go back to it
        }, 3000) // 3000 milliseconds = 3 seconds
    }
}
