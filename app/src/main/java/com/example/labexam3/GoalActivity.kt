package com.example.labexam3

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
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

        goalsListContainer.removeAllViews()

        for (goal in goalList) {
            val parts = goal.split(" | ")
            if (parts.size == 3) {
                val (name, amount, deadline) = parts

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


}
