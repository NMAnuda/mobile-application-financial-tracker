package com.example.labexam3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameInput: TextInputEditText = findViewById(R.id.email_input)
        val passwordInput: TextInputEditText = findViewById(R.id.password_input)
        val loginButton: Button = findViewById(R.id.login_button)

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val forget: TextView = findViewById(R.id.forgot_password_text)

        forget.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val savedUsername = sharedPreferences.getString("username", null)
            val savedPassword = sharedPreferences.getString("password", null)

            if (savedUsername == null || savedPassword == null) {
                Toast.makeText(this, "No account found. Please sign up first.", Toast.LENGTH_SHORT).show()
            } else if (username == savedUsername && password == savedPassword) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
