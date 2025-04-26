package com.example.labexam3

import android.content.Context
import android.net.Uri
import android.os.Bundle import android.view.LayoutInflater import android.view.View import android.view.ViewGroup import android.widget.Button
import android.widget.ImageView
import android.widget.TextView import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment import com.google.android.material.textfield.TextInputEditText

class SettingsFragment : Fragment() {
    private lateinit var profileImage: ImageView
    private lateinit var usernameHeader: TextView
    private lateinit var emailHeader: TextView
    private lateinit var emailInput: TextInputEditText
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var editButton: Button
    private lateinit var saveButton: Button
    private var imageUri: Uri? = null

    // Activity result launcher for image selection
    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            profileImage.setImageURI(it)
            // Save the URI to SharedPreferences
            val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("profile_image_uri", it.toString()).apply()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize views
        profileImage = view.findViewById(R.id.profileImage)
        usernameHeader = view.findViewById(R.id.usernameHeader)
        emailHeader = view.findViewById(R.id.emailHeader)
        emailInput = view.findViewById(R.id.emailInput)
        usernameInput = view.findViewById(R.id.usernameInput)
        passwordInput = view.findViewById(R.id.passwordInput)
        editButton = view.findViewById(R.id.editButton)
        saveButton = view.findViewById(R.id.saveButton)

        // Load user data from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "user@example.com") ?: "user@example.com"
        val username = sharedPreferences.getString("username", "Username") ?: "Username"
        val password = sharedPreferences.getString("password", "********") ?: "********"
        val savedImageUri = sharedPreferences.getString("profile_image_uri", null)

        // Display user data
        usernameHeader.text = username
        emailHeader.text = email
        emailInput.setText(email)
        usernameInput.setText(username)
        passwordInput.setText(password)

        // Load profile image if available
        if (savedImageUri != null) {
            imageUri = Uri.parse(savedImageUri)
            profileImage.setImageURI(imageUri)
        }

        // Profile image click listener
        profileImage.setOnClickListener {
            // Launch gallery to pick an image
            imagePicker.launch("image/*")
        }

        // Edit button click listener
        editButton.setOnClickListener {
            emailInput.isEnabled = true
            usernameInput.isEnabled = true
            passwordInput.isEnabled = true
            editButton.visibility = View.GONE
            saveButton.visibility = View.VISIBLE
        }

        // Save button click listener
        saveButton.setOnClickListener {
            val newEmail = emailInput.text.toString().trim()
            val newUsername = usernameInput.text.toString().trim()
            val newPassword = passwordInput.text.toString().trim()

            if (newEmail.isEmpty() || newUsername.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val savedUsername = sharedPreferences.getString("username", null)
            if (newUsername != savedUsername && sharedPreferences.contains("username")) {
                val allUsernames = sharedPreferences.getString("username", null)
                if (allUsernames == newUsername) {
                    Toast.makeText(requireContext(), "Username already exists", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val editor = sharedPreferences.edit()
            editor.putString("email", newEmail)
            editor.putString("username", newUsername)
            editor.putString("password", newPassword)
            editor.apply()

            usernameHeader.text = newUsername
            emailHeader.text = newEmail

            emailInput.isEnabled = false
            usernameInput.isEnabled = false
            passwordInput.isEnabled = false

            editButton.visibility = View.VISIBLE
            saveButton.visibility = View.GONE

            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}