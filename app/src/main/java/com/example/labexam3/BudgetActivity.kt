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
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
    private var notificationPermissionRequested = false

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

            // Request POST_NOTIFICATIONS permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionRequested) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
                notificationPermissionRequested = true
            }

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
            finish()
        }
    }

    override fun onResume() {
        super.onResume() // Fixed from onResume34
        loadBudgetSummary()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("BudgetActivity", "POST_NOTIFICATIONS permission granted")
                // Retry loading budget summary to trigger notification if needed
                loadBudgetSummary()
            } else {
                Log.e("BudgetActivity", "POST_NOTIFICATIONS permission denied")
                Toast.makeText(
                    this,
                    "Notifications disabled. Enable in settings to receive budget alerts.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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

            val income = sharedPreferences.getFloat("income", 0f)
            val expenses = sharedPreferences.getFloat("expenses", 0f)
            val savings = sharedPreferences.getFloat("savings", 0f)
            val budgetPercentage = sharedPreferences.getFloat("budget_percentage", 50f)


            val budget = income * (budgetPercentage / 100f)
            val remainingBudget = budget - expenses
            val savingsAllocation = income - budget


            if (remainingBudget <= 1000f) {
                Log.d("BudgetActivity", "Remaining budget is $remainingBudget, checking notification")
                scheduleBudgetNotification(remainingBudget)
            } else {
                editor.putBoolean("budget_notification_sent", false)
                editor.apply()
                Log.d("BudgetActivity", "Remaining budget is $remainingBudget, reset notification flag")
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
                Log.d("BudgetActivity", "Notification channel created")
            }
        } catch (e: Exception) {
            Log.e("BudgetActivity", "Error creating notification channel: ${e.message}", e)
        }
    }

    private fun scheduleBudgetNotification(remainingBudget: Float) {
        try {
            // Check if notification was already sent
            val notificationSent = sharedPreferences.getBoolean("budget_notification_sent", false)
            if (notificationSent) {
                Log.d("BudgetActivity", "Notification already sent for budget <= 1000, skipping")
                return
            }

            // Check POST_NOTIFICATIONS permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("BudgetActivity", "POST_NOTIFICATIONS not granted, skipping notification")
                return
            }

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager == null) {
                Log.e("BudgetActivity", "AlarmManager is null")
                Toast.makeText(this, "Cannot schedule notification: Alarm service unavailable.", Toast.LENGTH_SHORT).show()
                return
            }

            // Check SCHEDULE_EXACT_ALARM permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.e("BudgetActivity", "Cannot schedule exact alarms, permission denied")
                Toast.makeText(
                    this,
                    "Please enable exact alarm permission in settings to receive budget alerts.",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = android.net.Uri.parse("package:$packageName")
                }
                startActivity(intent)
                return
            }

            val intent = Intent(this, BudgetNotificationReceiver::class.java).apply {
                putExtra("remainingBudget", remainingBudget)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                1000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule notification with a 1-second delay
            val triggerTime = System.currentTimeMillis() + 1000
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("BudgetActivity", "Notification scheduled for remaining budget: $remainingBudget")

            // Mark notification as sent
            editor.putBoolean("budget_notification_sent", true)
            editor.apply()
        } catch (e: SecurityException) {
            Log.e("BudgetActivity", "SecurityException in scheduleBudgetNotification: ${e.message}", e)
            Toast.makeText(
                this,
                "Notification permission issue. Please check app settings.",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Log.e("BudgetActivity", "Error in scheduleBudgetNotification: ${e.message}", e)
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

            notificationManager.notify(1000, notification)
            Log.d("BudgetActivity", "Notification displayed for remaining budget: $remainingBudget")
        } catch (e: Exception) {
            Log.e("BudgetActivity", "Error in BudgetNotificationReceiver: ${e.message}", e)
        }
    }
}