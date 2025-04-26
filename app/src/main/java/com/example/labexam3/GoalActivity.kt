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
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.*

class GoalActivity : AppCompatActivity() {

    private lateinit var goalsListContainer: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var addGoalButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal)

        goalsListContainer = findViewById(R.id.goalsListContainer)
        addGoalButton = findViewById(R.id.addGoalButton)
        sharedPreferences = getSharedPreferences("financeData", MODE_PRIVATE)

        // Create notification channel
        createNotificationChannel()

        // Dummy data if needed
        if (sharedPreferences.getString("goals", "").isNullOrEmpty()) {
            val dummyGoals = "Buy Bike | 100000 | 2025-06-01\nSave for Uni | 250000 | 2025-12-31"
            sharedPreferences.edit().putString("goals", dummyGoals).apply()
        }

        loadGoals()

        addGoalButton.setOnClickListener {
            startActivity(Intent(this, AddGoalActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadGoals()
    }

    private fun loadGoals() {
        val goals = sharedPreferences.getString("goals", "") ?: ""
        val goalList = goals.split("\n").filter { it.isNotBlank() }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = Calendar.getInstance()

        goalsListContainer.removeAllViews()

        for ((index, goal) in goalList.withIndex()) {
            val parts = goal.split(" | ")
            if (parts.size == 3) {
                val (name, amount, deadline) = parts

                // Parse deadline and schedule notification
                try {
                    val deadlineDate = dateFormat.parse(deadline)
                    if (deadlineDate != null) {
                        val deadlineCalendar = Calendar.getInstance().apply { time = deadlineDate }
                        if (!deadlineCalendar.before(currentDate)) {
                            scheduleNotification(index, name, deadlineCalendar.timeInMillis)
                        }
                    }
                } catch (e: Exception) {
                    // Handle parsing error if needed
                }

                val card = CardView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(32, 16, 32, 16)
                    }
                    radius = 16f
                    cardElevation = 10f
                    setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                }

                val innerLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(40, 40, 40, 40)
                }

                val nameView = TextView(this).apply {
                    text = "🏷 Goal: $name"
                    textSize = 20f
                    setTextColor(ContextCompat.getColor(context, android.R.color.black))
                }

                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        2
                    ).apply {
                        topMargin = 16
                        bottomMargin = 16
                    }
                    setBackgroundColor(0x22000000)
                }

                val amountView = TextView(this).apply {
                    text = "💰 Amount: $amount"
                    textSize = 18f
                    setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                }

                val deadlineView = TextView(this).apply {
                    text = "📅 Deadline: $deadline"
                    textSize = 18f
                    setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                    setPadding(0, 8, 0, 0)
                }

                innerLayout.addView(nameView)
                innerLayout.addView(divider)
                innerLayout.addView(amountView)
                innerLayout.addView(deadlineView)
                card.addView(innerLayout)

                goalsListContainer.addView(card)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Goal Reminders"
            val descriptionText = "Notifications for goal deadlines"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("GOAL_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(goalId: Int, goalName: String, deadlineTime: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, GoalNotificationReceiver::class.java).apply {
            putExtra("goalName", goalName)
            putExtra("goalId", goalId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            goalId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm for the exact deadline
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            deadlineTime,
            pendingIntent
        )
    }
}

class GoalNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val goalName = intent.getStringExtra("goalName") ?: "Goal"
        val goalId = intent.getIntExtra("goalId", 0)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, "GOAL_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Goal Deadline Reached")
            .setContentText("The deadline for your goal '$goalName' has arrived!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(goalId, notification)
    }
}