package com.example.labexam3

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val emailInput: TextInputEditText = findViewById(R.id.email_input)
        val usernameInput: TextInputEditText = findViewById(R.id.name_input)
        val passwordInput: TextInputEditText = findViewById(R.id.password_input)
        val signupButton: Button = findViewById(R.id.signup_button)

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        signupButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val savedUsername = sharedPreferences.getString("username", null)
            if (savedUsername == username) {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            editor.putString("email", email)
            editor.putString("username", username)
            editor.putString("password", password)
            editor.apply()
            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()

            finish()
        }
    }
}
