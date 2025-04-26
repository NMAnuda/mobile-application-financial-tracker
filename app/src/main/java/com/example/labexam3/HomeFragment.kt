package com.example.labexam3

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {
    private lateinit var balanceText: TextView
    private lateinit var incomeText: TextView
    private lateinit var expenseText: TextView
    private lateinit var transactionsContainer: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var addButton: FloatingActionButton
    private lateinit var emailText: TextView
    private lateinit var transactionLimitText: TextView

    private lateinit var userPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_main, container, false)

        // Initialize UI elements
        balanceText = view.findViewById(R.id.balanceText)
        incomeText = view.findViewById(R.id.incomeText)
        expenseText = view.findViewById(R.id.expenseText)
        emailText = view.findViewById(R.id.emailText)
        transactionLimitText = view.findViewById(R.id.transactionLimitText)
        addButton = view.findViewById(R.id.addButton)
        transactionsContainer = view.findViewById<ScrollView>(R.id.recentTransactionsContainer)
            .findViewById(R.id.transactionsInnerContainer)

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("financeData", AppCompatActivity.MODE_PRIVATE)
        userPrefs = requireActivity().getSharedPreferences("UserPrefs", AppCompatActivity.MODE_PRIVATE)

        // Set up button click listeners
        view.findViewById<Button>(R.id.HistorylButton).setOnClickListener {
            startActivity(Intent(requireContext(), AllTransactionsActivity::class.java))
        }

        view.findViewById<Button>(R.id.GoalButton).setOnClickListener {
            startActivity(Intent(requireContext(), GoalActivity::class.java))
        }

        view.findViewById<Button>(R.id.budgetButton).setOnClickListener {
            startActivity(Intent(requireContext(), BudgetActivity::class.java))
        }

        addButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddTransactionActivity::class.java))
        }

        loadSummary()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadSummary()
    }

    private fun loadSummary() {
        // Fetch financial data
        val income = sharedPreferences.getFloat("income", 0f)
        val expenses = sharedPreferences.getFloat("expenses", 0f)
        val savings = sharedPreferences.getFloat("savings", 0f)
        val budgetPercentage = sharedPreferences.getFloat("budget_percentage", 50f)
        val transactions = sharedPreferences.getString("transactions", "") ?: ""

        // Fetch email from UserPrefs
        val email = userPrefs.getString("email", null)
        emailText.text = email ?: "Not signed in"

        // Calculate and display financial data
        val balance = income - expenses - savings
        val budget = income * (budgetPercentage / 100f)
        val remainingBudget = budget - expenses

        balanceText.text = "Balance: Rs. $balance"
        incomeText.text = "Income: Rs. $income"
        expenseText.text = "Expenses: Rs. $expenses"
        transactionLimitText.text = "Remaining Transaction Limit: Rs. ${remainingBudget.coerceAtLeast(0f)}"

        // Display recent transactions
        transactionsContainer.removeAllViews()
        val transactionList = transactions.split("\n").filter { it.isNotBlank() }

        transactionList.forEach { transaction ->
            val cardLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
                background = resources.getDrawable(R.drawable.transaction_card_background, null)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 24)
                layoutParams = params
            }

            val transactionText = TextView(requireContext()).apply {
                text = transaction
                textSize = 16f
                setTextColor(resources.getColor(R.color.text_primary, null))
            }

            cardLayout.addView(transactionText)
            transactionsContainer.addView(cardLayout)
        }

        // Show placeholder if no transactions exist
        if (transactionList.isEmpty()) {
            val placeholder = TextView(requireContext()).apply {
                text = "No transactions yet"
                textSize = 16f
                setTextColor(resources.getColor(R.color.text_secondary, null))
                setPadding(16, 12, 16, 12)
            }
            transactionsContainer.addView(placeholder)
        }
    }
}