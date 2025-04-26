package com.example.labexam3

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class BudgetActivity : AppCompatActivity() {
    private lateinit var totalIncomeText: TextView
    private lateinit var budgetText: TextView
    private lateinit var expensesText: TextView
    private lateinit var remainingBudgetText: TextView
    private lateinit var savingsText: TextView
    private lateinit var budgetPercentageInput: TextInputEditText
    private lateinit var saveBudgetButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_budget)

            // Initialize UI elements
            totalIncomeText = findViewById(R.id.totalIncomeText)
            budgetText = findViewById(R.id.budgetText)
            expensesText = findViewById(R.id.expensesText)
            remainingBudgetText = findViewById(R.id.remainingBudgetText)
            savingsText = findViewById(R.id.savingsText)
            budgetPercentageInput = findViewById(R.id.budgetPercentageInput)
            saveBudgetButton = findViewById(R.id.saveBudgetButton)

            // Initialize SharedPreferences
            sharedPreferences = getSharedPreferences("financeData", MODE_PRIVATE)
            editor = sharedPreferences.edit()

            // Create notification channel
            createNotificationChannel()

            // Load initial budget percentage (default to 50% if not set)
            val budgetPercentage = sharedPreferences.getFloat("budget_percentage", 50f)
            budgetPercentageInput.setText(budgetPercentage.toString())

            // Set up save button listener
            saveBudgetButton.setOnClickListener {
                saveBudgetPercentage()
            }

            // Load and display budget summary
            loadBudgetSummary()
        } catch (e: Exception) {
            Log.e("BudgetActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading budget page. Please try again.", Toast.LENGTH_LONG).show()
            finish() // Return to previous activity
        }
    }

    override fun onResume() {
        super.onResume()
        loadBudgetSummary()
    }

    private fun saveBudgetPercentage() {
        try {
            val percentageText = budgetPercentageInput.text.toString()
            val percentage = percentageText.toFloatOrNull()

            if (percentage == null || percentage < 0 || percentage > 100) {
                Toast.makeText(this, "Please enter a valid percentage (0-100)", Toast.LENGTH_SHORT).show()
                return
            }

            editor.putFloat("budget_percentage", percentage)
            editor.apply()

            Toast.makeText(this, "Budget percentage updated to $percentage%", Toast.LENGTH_SHORT).show()
            loadBudgetSummary()
        } catch (e: Exception) {
            Log.e("BudgetActivity", "Error saving budget percentage: ${e.message}", e)
            Toast.makeText(this, "Error saving budget percentage.", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadBudgetSummary() {
        try {
            // Fetch financial data
            val income = sharedPreferences.getFloat("income", 0f)
            val expenses = sharedPreferences.getFloat("expenses", 0f)
            val savings = sharedPreferences.getFloat("savings", 0f)
            val budgetPercentage = sharedPreferences.getFloat("budget_percentage", 50f)

            // Calculate budget and remaining budget
            val budget = income * (budgetPercentage / 100f)
            val remainingBudget = budget - expenses
            val savingsAllocation = income - budget

            // Check if remaining budget is <= 1000 and schedule notification
            if (remainingBudget <= 1000f) {
                Log.d("BudgetActivity", "Remaining budget is $remainingBudget, scheduling notification")
                scheduleBudgetNotification(remainingBudget)
            }

            // Update UI
            totalIncomeText.text = "Total Income: Rs. $income"
            budgetText.text = "Rs. ${remainingBudget.coerceAtLeast(0f)}"
            expensesText.text = "Expenses: Rs. $expenses"
            remainingBudgetText.text = "Budget ($budgetPercentage%): Rs. $budget"
            savingsText.text = "Rs. $savingsAllocation"

            // Update budget percentage input
            budgetPercentageInput.setText(budgetPercentage.toString())
        } catch (e: Exception) {
            Log.e("BudgetActivity", "Error in loadBudgetSummary: ${e.message}", e)
            Toast.makeText(this, "Error loading budget data.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Budget Alerts"
                val descriptionText = "Notifications for low budget alerts"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel("BUDGET_CHANNEL", name, importance).apply {
                    description = descriptionText
                }
                val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        } catch (e: Exception) {
            Log.e("BudgetActivity", "Error creating notification channel: ${e.message}", e)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleBudgetNotification(remainingBudget: Float) {
        try {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, BudgetNotificationReceiver::class.java).apply {
                putExtra("remainingBudget", remainingBudget)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                1000, // Unique request code for budget notification
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule notification to trigger immediately (or in a few seconds)
            val triggerTime = System.currentTimeMillis() + 1000 // 1 second delay
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("BudgetActivity", "Notification scheduled for remaining budget: $remainingBudget")
        } catch (e: Exception) {
            Log.e("BudgetActivity", "Error scheduling notification: ${e.message}", e)
            Toast.makeText(this, "Error scheduling budget notification.", Toast.LENGTH_SHORT).show()
        }
    }
}

class BudgetNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val remainingBudget = intent.getFloatExtra("remainingBudget", 0f)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(context, "BUDGET_CHANNEL")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Low Budget Alert")
                .setContentText("Your remaining budget is Rs. $remainingBudget! Consider reviewing your expenses.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(1000, notification) // Unique ID for budget notification
            Log.d("BudgetActivity", "Notification displayed for remaining budget: $remainingBudget")
        } catch (e: Exception) {
            Log.e("BudgetActivity", "Error in BudgetNotificationReceiver: ${e.message}", e)
        }
    }
}