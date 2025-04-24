package com.example.labexam3

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.labexam3.databinding.ActivityMainBinding

import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var balanceText: TextView
    private lateinit var incomeText: TextView
    private lateinit var expenseText: TextView
    private lateinit var transactionsContainer: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var addButton: FloatingActionButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        balanceText = findViewById(R.id.balanceText)
        incomeText = findViewById(R.id.incomeText)
        expenseText = findViewById(R.id.expenseText)
        addButton = findViewById(R.id.addButton)
        transactionsContainer = findViewById<ScrollView>(R.id.recentTransactionsContainer)
            .findViewById(R.id.transactionsInnerContainer)

        sharedPreferences = getSharedPreferences("financeData", MODE_PRIVATE)

        findViewById<Button>(R.id.HistorylButton).setOnClickListener {
            startActivity(Intent(this, AllTransactionsActivity::class.java))
        }

        findViewById<Button>(R.id.GoalButton).setOnClickListener {
            startActivity(Intent(this, GoalActivity::class.java))
        }

        addButton.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }



        loadSummary()
    }

    override fun onResume() {
        super.onResume()
        loadSummary()
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    private fun loadSummary() {
        val income = sharedPreferences.getFloat("income", 0f)
        val expenses = sharedPreferences.getFloat("expenses", 0f)
        val transactions = sharedPreferences.getString("transactions", "") ?: ""

        val balance = income - expenses

        balanceText.text = "Balance: Rs. $balance"
        incomeText.text = "Income: Rs. $income"
        expenseText.text = "Expenses: Rs. $expenses"

        transactionsContainer.removeAllViews()
        val transactionList = transactions.split("\n").filter { it.isNotBlank() }

        transactionList.forEach { transaction ->
            val cardLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
                background = resources.getDrawable(R.drawable.transaction_card_background, theme)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 24)
                layoutParams = params
            }

            val transactionText = TextView(this).apply {
                text = transaction
                textSize = 16f
                setTextColor(resources.getColor(R.color.text_primary, theme))
            }

            cardLayout.addView(transactionText)
            transactionsContainer.addView(cardLayout)
        }

        if (transactionList.isEmpty()) {
            val placeholder = TextView(this).apply {
                text = "No transactions yet"
                textSize = 16f
                setTextColor(resources.getColor(R.color.text_secondary, theme))
                setPadding(16, 12, 16, 12)
            }
            transactionsContainer.addView(placeholder)
        }
    }
}
