package com.example.labexam3

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class AllTransactionsActivity : AppCompatActivity() {

    private lateinit var transactionsContainer: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_transactions)

        transactionsContainer = findViewById<LinearLayout>(R.id.transactionsInnerContainer)
        sharedPreferences = getSharedPreferences("financeData", MODE_PRIVATE)

        loadTransactions()
    }

    private fun loadTransactions() {
        val transactions = sharedPreferences.getString("transactions", "") ?: ""
        Log.d("TRANSACTION_DEBUG", "Stored transactions: $transactions")

        val transactionList = transactions.split("\n").filter { it.isNotBlank() }

        transactionsContainer.removeAllViews()

        transactionList.forEach { transaction ->
            val parts = transaction.split(": ", " - ")
            val type = parts.getOrNull(0) ?: "Unknown"
            val amount = parts.getOrNull(1) ?: "Rs. 0"
            val description = parts.getOrNull(2) ?: "No description"

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

            val typeView = TextView(this).apply {
                text = "🏷 Type: $type"
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
                setBackgroundColor(0x22000000) // Semi-transparent divider
            }

            val amountView = TextView(this).apply {
                text = "💰 Amount: $amount"
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            }

            val descriptionView = TextView(this).apply {
                text = "📋 Description: $description"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                setPadding(0, 8, 0, 0)
            }

            val editButton = ImageButton(this).apply {
                setImageResource(R.drawable.ic_edit)
                background = null
                setOnClickListener {
                    val intent = Intent(context, EditTransactionActivity::class.java)
                    intent.putExtra("transaction", transaction)
                    startActivity(intent)
                }
            }

            innerLayout.addView(typeView)
            innerLayout.addView(divider)
            innerLayout.addView(amountView)
            innerLayout.addView(descriptionView)
            innerLayout.addView(editButton)
            card.addView(innerLayout)

            transactionsContainer.addView(card)
        }
    }
}
