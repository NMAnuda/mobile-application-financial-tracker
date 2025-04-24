package com.example.labexam3

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class AddGoalActivity : AppCompatActivity() {

    private lateinit var goalNameInput: EditText
    private lateinit var goalAmountInput: EditText
    private lateinit var goalDeadlineInput: EditText
    private lateinit var saveGoalButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_goal)

        goalNameInput = findViewById(R.id.goalNameInput)
        goalAmountInput = findViewById(R.id.goalAmountInput)
        goalDeadlineInput = findViewById(R.id.goalDeadlineInput)
        saveGoalButton = findViewById(R.id.saveGoalButton)
        sharedPreferences = getSharedPreferences("financeData", MODE_PRIVATE)

        // Show date picker
        goalDeadlineInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    goalDeadlineInput.setText(formattedDate)
                }, year, month, day
            )
            datePickerDialog.show()
        }

        saveGoalButton.setOnClickListener {
            val goalName = goalNameInput.text.toString().trim()
            val goalAmount = goalAmountInput.text.toString().trim()
            val goalDeadline = goalDeadlineInput.text.toString().trim()

            if (goalName.isNotEmpty() && goalAmount.isNotEmpty() && goalDeadline.isNotEmpty()) {
                // Format: "Name | Amount | Deadline"
                val newGoal = "$goalName | $goalAmount | $goalDeadline"
                val currentGoals = sharedPreferences.getString("goals", "") ?: ""
                val updatedGoals = if (currentGoals.isEmpty()) newGoal else "$currentGoals\n$newGoal"

                sharedPreferences.edit().putString("goals", updatedGoals).apply()
                finish()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
